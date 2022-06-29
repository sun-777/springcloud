package com.sun.server.mapper;

import com.sun.common.entity.Entity;
import com.sun.common.id.CustomIdGenerator;
import com.sun.server.mapper.handler.SqlBatchHandler;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.persister.entity.EntityPersister;

import javax.persistence.EntityManager;
import javax.persistence.TransactionRequiredException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * @description: 实体映射抽象类，在AbstractEntityMapper类的基础上加入批处理方法
 *
 * @author: Sun Xiaodong
 */
public abstract class AbstractEntityBatchMapper<T extends Entity> extends AbstractEntityMapper<T> implements BatchMapper<T> {


    public final SqlBatchHandler<T> SAVE_BATCH_HANDLER = (entities, conn, batchSize) -> doExecute(entities, conn, batchSize, BatchType.INSERT);
    public final SqlBatchHandler<T> UPDATE_BATCH_HANDLER = (entities, conn, batchSize) -> doExecute(entities, conn, batchSize, BatchType.UPDATE);
    public final SqlBatchHandler<T> DELETE_BATCH_HANDLER = (entities, conn, batchSize) -> doExecute(entities, conn, batchSize, BatchType.DELETE);


    abstract String getPreparedSql(final BatchType type);

    abstract void setParameters(final PreparedStatement ps, final T t, final BatchType type) throws SQLException;


    @Override
    public List<Integer> batchSave(List<T> entities) {
        return doBatchHandler(entities, SAVE_BATCH_HANDLER, true);
    }


    @Override
    public List<Integer> batchUpdate(List<T> entities) {
        return doBatchHandler(entities, UPDATE_BATCH_HANDLER);
    }


    @Override
    public List<Integer> batchDelete(List<T> entities) {
        return doBatchHandler(entities, DELETE_BATCH_HANDLER);
    }


    protected List<Integer> doBatchHandler(final List<T> entities, final SqlBatchHandler<T> handler) {
        return doBatchHandler(entities, handler, false);
    }



    protected List<Integer> doBatchHandler(final List<T> entities, final SqlBatchHandler<T> handler, final boolean save) {
        if (null == entities || null == handler || entities.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            final EntityManager em = getEntityManager();

            // 获得Hibernate API中的Session
            final Session session = em.unwrap(Session.class);

            // 如果是批量保存，则需要执行生成主键id
            if (save) {
                // 获得当前类的泛型的类文字
                @SuppressWarnings("unchecked")
                final Class<T> clazz = (Class<T>)((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                final EntityPersister persister = this.getEntityPersister(clazz);
                final IdentifierGenerator identifierGenerator = persister.getIdentifierGenerator();
                // 如果是自定义主键CustomIdGenerator，则插入数据库表之前，给所有的实体对象生成id
                if (identifierGenerator instanceof CustomIdGenerator) {
                    for (T entity : entities) {
                        Serializable generatedId = persister.getIdentifierGenerator().generate(null, entity);
                        persister.getEntityTuplizer().setIdentifier( entity, generatedId, null);
                    }
                }
            }

            // 通过JDBC原生API，实现高效批处理
            return session.doReturningWork(conn -> {
                final int[] results = handler.doBatch(entities, conn, getBatchSize());
                return IntStream.of(results).boxed().collect(Collectors.toUnmodifiableList());
            });
        } catch (HibernateException | TransactionRequiredException | IllegalArgumentException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getCause());
        } finally {
            /**
             // auto close by Springboot framework
             if (null != em && em.isOpen()) {
             em.close();
             }*/
        }
    }


    // 执行批处理
    private int[] doExecute(List<T> entities, Connection conn, int batchSize, BatchType type) throws SQLException {
        try (final PreparedStatement ps = conn.prepareStatement(getPreparedSql(type))) {
            final int size = entities.size();
            // 批处理结果数组
            final int[] result = new int[size];

            // 记录当前 ps.executeBatch()的数据的数量
            int pos = 0;
            for (int i = 0; i < size; i++) {
                // 写入参数
                setParameters(ps, entities.get(i), type);
                ps.addBatch();

                // 到达Hibernate中设置的批处理大小时，执行
                if (0 != i && 0 == (i + 1) % batchSize) {
                    // 返回值：
                    //      1、大于等于0： 指示成功处理了命令，是给出执行命令所影响数据库中行数的更新计数。
                    //      2、-2(SUCCESS_NO_INFO): 指示成功执行了命令，但是由于某一个记录执行失败（抛出BatchUpdateException异常）导致受影响的行数是未知的。
                    //      3、-3(EXECUTE_FAILED): 抛出BatchUpdateException异常。
                    //                             发生失败后，如果驱动继续执行，通过BatchUpdateException.getUpdateCounts()方法返回的数组应该包括批处理的结果。
                    final int[] batch = ps.executeBatch();
                    // 拷贝结果到result
                    System.arraycopy(batch, 0, result, pos, batch.length);
                    pos += batch.length;
                }
            }

            if (pos < size) {
                int[] batch = ps.executeBatch();
                // 拷贝结果到result
                System.arraycopy(batch, 0, result, pos, batch.length);
                pos += batch.length;
            }

            return result;
        }
    }


}

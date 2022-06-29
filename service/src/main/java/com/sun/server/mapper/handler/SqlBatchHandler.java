package com.sun.server.mapper.handler;

import com.sun.common.entity.Entity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @description: Hibernate持久框架中，执行批处理接口
 * @author: Sun Xiaodong
 */

@FunctionalInterface
public interface SqlBatchHandler<T extends Entity> {
    /**
     * 通过JDBC原生API，实现高效的批处理（包括批量保存、更新、删除等）
     * @param entities  批处理的实体类集合
     * @param conn  数据库连接
     * @param batchSize  批处理大小
     * @return  批处理结果数组
     */
    default int[] doBatch(final List<T> entities, final Connection conn, final int batchSize) throws SQLException {
        // 获取自动提交设置
        final boolean autoCommit = conn.getAutoCommit();
        // 如果自动提交为true，则设置为手动提交
        if (autoCommit) {
            conn.setAutoCommit(false);
        }
        
        try {
            final int[] result = execute(entities, conn, batchSize);
            // 提交
            conn.commit();
            return result;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
            throw e;
        } finally {
            // 恢复自动提交设置
            if (autoCommit) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    int[] execute(final List<T> entities, final Connection conn, final int batchSize) throws SQLException;
}

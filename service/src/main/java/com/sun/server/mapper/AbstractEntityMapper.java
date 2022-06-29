package com.sun.server.mapper;

import com.sun.common.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * @description: 实体映射抽象类，实现部分通用方法
 * @author: Sun Xiaodong
 */

public abstract class AbstractEntityMapper<T extends Entity> implements EntityMapper<T> {
    static final Logger LOG = LoggerFactory.getLogger(AbstractEntityMapper.class);

    @Override
    public void update(T entity) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.merge(entity);
        } catch (TransactionRequiredException | IllegalArgumentException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            /**
             // auto close by Springboot framework
             if (null != em && em.isOpen()) {
             em.close();
             }*/
        }
    }


    @Override
    public T saveOrUpdate(T entity) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            // 新增
            em.persist(entity);
            // 保证对象已经持久化
            //em.flush();
            return entity;
        } catch (EntityExistsException e) {
            // 如果保存失败，则更新
            entity = em.merge(entity);
            em.persist(entity);
            return entity;
        } catch (IllegalArgumentException | TransactionRequiredException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            /**
             // auto close by Springboot framework
             if (null != em && em.isOpen()) {
             em.close();
             }*/
        }
    }


    @Override
    public T load(Class<T> clazz, Object primaryKey) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            // 和Hibernate load()基本相同
            // 区别是：当数据库中不存在与参数 primaryKey对应的记录时，Session的load()方法抛出org.hibernate.ObjectNotFoundException；
            //       而EntityManager的 getReference()方法抛出javax.persistence.EntityNotFoundException
            return em.getReference(clazz, primaryKey);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            /**
             // auto close by Springboot framework
             if (null != em && em.isOpen()) {
             em.close();
             }*/
        }
    }


    @Override
    public T get(Class<T> clazz, Object primaryKey) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            return em.find(clazz, primaryKey);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            /**
             // auto close by Springboot framework
             if (null != em && em.isOpen()) {
             em.close();
             }*/
        }
    }


    @Override
    public T save(T entity) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            // 将一个游离对象或临时对象的属性复制到一个持久化对象中，EntityManager接口没有save()方法，只有Session接口具有save()方法
            em.persist(entity);
            // Caused by: javax.persistence.TransactionRequiredException: no transaction is in progress
            // See: https://developer.jboss.org/thread/278691
            //
            // Hibernate ORM 5.2+
            // Hibernate now conforms with the JPA specification to not allow flushing updates outside of a transaction boundary.
            // To restore 5.1 behavior, allowing flush operations outside of a transaction boundary,
            // set hibernate.allow_update_outside_transaction=true.
            em.flush();
            return entity;
        } catch (IllegalArgumentException | TransactionRequiredException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            /**
             // auto close by Springboot framework
             if (null != em && em.isOpen()) {
             em.close();
             }*/
        }
    }


    @Override
    public void persist(T entity) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.persist(entity);
            // 保证对象已经持久化
            // em.flush();
        } catch (IllegalArgumentException | TransactionRequiredException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            /**
             // auto close by Springboot framework
             if (null != em && em.isOpen()) {
             em.close();
             }*/
        }
    }


    @Override
    public void delete(T entity) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            // 和Hibernate delete()方法基本相同
            // 区别是：Session的 delete()方法可以删除持久化对象和游离对象；
            //       而EntityManager的remove()方法只能删除持久化对象。
            em.remove(entity);
        } catch (IllegalArgumentException | TransactionRequiredException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            /**
             // auto close by Springboot framework
             if (null != em && em.isOpen()) {
             em.close();
             }*/
        }
    }


    @Override
    public List<T> getRange(Class<T> clazz, int startPosition, int maxResults) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            final CriteriaBuilder builder = em.getCriteriaBuilder();
            // 设置查询结果类型
            final CriteriaQuery<T> criteria = builder.createQuery(clazz);
            final Root<T> from = criteria.from(clazz);
            // 查询实体类的所有字段
            final CriteriaQuery<T> select = criteria.select(from);
            // 设置条件
            final TypedQuery<T> query = em.createQuery(select);
            query.setFirstResult(startPosition).setMaxResults(maxResults);
            // 返回查询结果
            return query.getResultList();
        } catch (IllegalArgumentException | PersistenceException e) {
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


    @Override
    public Long count(Class<T> clazz) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            final CriteriaBuilder builder = em.getCriteriaBuilder();
            // 设置查询结果类型
            final CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
            final Root<T> root = criteria.from(clazz);
            // 设置查询的字段
            criteria.select(builder.count(root));
            // 设置查询条件
            final TypedQuery<Long> query = em.createQuery(criteria);
            // 返回查询结果
            return query.getSingleResult();
        } catch (IllegalArgumentException | PersistenceException | IllegalStateException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            /**
            // auto close by Springboot framework
            if (null != em && em.isOpen()) {
                em.close();
            }*/
        }
    }


}

package com.force.bookstore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
public class AbstractEntityService<T extends Persistable> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractEntityService.class);

    Class<T> tClass;

    @PersistenceContext
    EntityManager em;

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    public void settClass(Class<T> tClass) {
        this.tClass = tClass;
    }

    @Transactional(readOnly = true)
    public T findEntity(String id) throws IllegalAccessException, InstantiationException {
        if ("new".equals(id)) {
            return tClass.newInstance();
        } else {
            return em.find(tClass, id);
        }
    }

    @Transactional
    public T save(T entity) {
        if (entity.getId() != null) {
            entity = em.merge(entity);
        } else {
            em.persist(entity);
        }
        logger.info("entity saved: " + em);
        return entity;

    }

    @Transactional
    public boolean delete(String entityId) {
        T entity = em.find(tClass, entityId);
        if (entity == null) {
            return false;
        }
        em.remove(entity);
        return true;
    }
}

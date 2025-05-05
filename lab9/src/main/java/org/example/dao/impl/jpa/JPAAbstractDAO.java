package org.example.dao.impl.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.example.dao.AbstractDAO;
import org.example.model.Country;
import org.example.utils.JPAUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

public abstract class JPAAbstractDAO<T, ID> implements AbstractDAO<T, ID> {
    protected final EntityManager entityManager;
    protected final Class<T> entityClass;
    protected final Logger logger;

    @SuppressWarnings("unchecked")
    public JPAAbstractDAO() {
        this.entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    public void create(T entity) {
        logger.debug("Creating new entity of type: {}", entityClass.getSimpleName());
        long startTime = System.currentTimeMillis();

        boolean isActiveTransaction = entityManager.getTransaction().isActive();
        if (!isActiveTransaction) {
            entityManager.getTransaction().begin();
        }

        try {
            entityManager.persist(entity);

            if (!isActiveTransaction) {
                entityManager.getTransaction().commit();
            }

            long endTime = System.currentTimeMillis();
            logger.info("Entity created in {}ms", endTime - startTime);
        } catch (Exception e) {
            if (!isActiveTransaction) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error creating entity: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating entity", e);
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        logger.debug("Finding {} with ID: {}", entityClass.getSimpleName(), id);
        long startTime = System.currentTimeMillis();

        try {
            T entity = entityManager.find(entityClass, id);
            long endTime = System.currentTimeMillis();
            logger.info("FindById query executed in {}ms", endTime - startTime);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            logger.error("Error finding entity by ID: {}", e.getMessage(), e);
            throw new RuntimeException("Error finding entity by ID", e);
        }
    }

    @Override
    public List<T> findAll() {
        logger.debug("Finding all entities of type: {}", entityClass.getSimpleName());
        long startTime = System.currentTimeMillis();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root);

            TypedQuery<T> query = entityManager.createQuery(cq);
            List<T> results = query.getResultList();

            long endTime = System.currentTimeMillis();
            logger.info("FindAll query executed in {}ms, found {} entities",
                    endTime - startTime, results.size());

            return results;
        } catch (Exception e) {
            logger.error("Error finding all entities: {}", e.getMessage(), e);
            throw new RuntimeException("Error finding all entities", e);
        }
    }

    @Override
    public T update(T entity) {
        logger.debug("Updating entity of type: {}", entityClass.getSimpleName());
        long startTime = System.currentTimeMillis();

        boolean isActiveTransaction = entityManager.getTransaction().isActive();
        if (!isActiveTransaction) {
            entityManager.getTransaction().begin();
        }

        try {
            T updatedEntity = entityManager.merge(entity);

            if (!isActiveTransaction) {
                entityManager.getTransaction().commit();
            }

            long endTime = System.currentTimeMillis();
            logger.info("Entity updated in {}ms", endTime - startTime);

            return updatedEntity;
        } catch (Exception e) {
            if (!isActiveTransaction) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error updating entity: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating entity", e);
        }
    }

    @Override
    public void delete(T entity) {
        logger.debug("Deleting entity of type: {}", entityClass.getSimpleName());
        long startTime = System.currentTimeMillis();

        boolean isActiveTransaction = entityManager.getTransaction().isActive();
        if (!isActiveTransaction) {
            entityManager.getTransaction().begin();
        }

        try {
            entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));

            if (!isActiveTransaction) {
                entityManager.getTransaction().commit();
            }

            long endTime = System.currentTimeMillis();
            logger.info("Entity deleted in {}ms", endTime - startTime);
        } catch (Exception e) {
            if (!isActiveTransaction) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting entity: {}", e.getMessage(), e);
            throw new RuntimeException("Error deleting entity", e);
        }
    }

    @Override
    public void deleteById(ID id) {
        logger.debug("Deleting {} with ID: {}", entityClass.getSimpleName(), id);
        findById(id).ifPresent(this::delete);
    }
}

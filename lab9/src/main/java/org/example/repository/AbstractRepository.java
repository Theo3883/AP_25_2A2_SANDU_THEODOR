
package org.example.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public abstract class AbstractRepository<T, ID> {

    protected final EntityManager entityManager;
    protected final Class<T> entityClass;
    protected final Logger logger;


    @SuppressWarnings("unchecked")
    public AbstractRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        this.logger = LoggerFactory.getLogger(getClass());
    }

    public Optional<T> findById(ID id) {
        logger.debug("Finding {} with ID: {}", entityClass.getSimpleName(), id);
        long startTime = System.currentTimeMillis();

        try {
            T result = entityManager.find(entityClass, id);
            long endTime = System.currentTimeMillis();
            logger.info("Query findById executed in {}ms", endTime - startTime);

            if (result == null) {
                logger.debug("No {} found with ID: {}", entityClass.getSimpleName(), id);
            } else {
                logger.debug("Found {} with ID: {}", entityClass.getSimpleName(), id);
            }

            return Optional.ofNullable(result);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("Error finding {} with ID: {} (execution time: {}ms)",
                    entityClass.getSimpleName(), id, endTime - startTime, e);
            throw new RuntimeException("Error finding entity by ID", e);
        }
    }


    public List<T> findAll() {
        logger.debug("Finding all entities of type: {}", entityClass.getSimpleName());
        long startTime = System.currentTimeMillis();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root);

            List<T> results = entityManager.createQuery(cq).getResultList();
            long endTime = System.currentTimeMillis();
            logger.info("Query findAll executed in {}ms, found {} entities",
                    endTime - startTime, results.size());

            return results;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("Error finding all entities of type: {} (execution time: {}ms)",
                    entityClass.getSimpleName(), endTime - startTime, e);
            throw new RuntimeException("Error retrieving all entities", e);
        }
    }


    public List<T> findAllOrderedBy(String orderByField) {
        logger.debug("Finding all entities of type: {} ordered by: {}",
                entityClass.getSimpleName(), orderByField);
        long startTime = System.currentTimeMillis();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root).orderBy(cb.asc(root.get(orderByField)));

            List<T> results = entityManager.createQuery(cq).getResultList();
            long endTime = System.currentTimeMillis();
            logger.info("Query findAllOrderedBy({}) executed in {}ms, found {} entities",
                    orderByField, endTime - startTime, results.size());

            return results;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("Error finding all entities of type: {} ordered by: {} (execution time: {}ms)",
                    entityClass.getSimpleName(), orderByField, endTime - startTime, e);
            throw new RuntimeException("Error retrieving ordered entities", e);
        }
    }

    public T save(T entity) {
        Object id = getEntityId(entity);
        boolean isNew = id == null;
        logger.debug("{} entity of type: {}{}",
                isNew ? "Saving new" : "Updating existing",
                entityClass.getSimpleName(),
                !isNew ? " with ID: " + id : "");
        long startTime = System.currentTimeMillis();

        try {
            T result;
            if (isNew) {
                entityManager.persist(entity);
                result = entity;
            } else {
                result = entityManager.merge(entity);
            }

            long endTime = System.currentTimeMillis();
            logger.info("Entity {} {} in {}ms",
                    isNew ? "persisted" : "merged",
                    entityClass.getSimpleName(),
                    endTime - startTime);

            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("Error {} entity of type: {} (execution time: {}ms)",
                    isNew ? "saving new" : "updating existing",
                    entityClass.getSimpleName(),
                    endTime - startTime, e);
            throw new RuntimeException("Error saving entity", e);
        }
    }


    public void delete(T entity) {
        Object id = getEntityId(entity);
        logger.debug("Deleting {} with ID: {}", entityClass.getSimpleName(), id);
        long startTime = System.currentTimeMillis();

        try {
            entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
            long endTime = System.currentTimeMillis();
            logger.info("Entity {} with ID: {} deleted in {}ms",
                    entityClass.getSimpleName(), id, endTime - startTime);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("Error deleting {} with ID: {} (execution time: {}ms)",
                    entityClass.getSimpleName(), id, endTime - startTime, e);
            throw new RuntimeException("Error deleting entity", e);
        }
    }


    public void deleteById(ID id) {
        logger.debug("Deleting {} with ID: {}", entityClass.getSimpleName(), id);
        long startTime = System.currentTimeMillis();

        try {
            findById(id).ifPresent(this::delete);
            long endTime = System.currentTimeMillis();
            logger.info("Entity {} with ID: {} deletion attempt completed in {}ms",
                    entityClass.getSimpleName(), id, endTime - startTime);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("Error deleting {} with ID: {} (execution time: {}ms)",
                    entityClass.getSimpleName(), id, endTime - startTime, e);
            throw new RuntimeException("Error deleting entity by ID", e);
        }
    }


    public long count() {
        logger.debug("Counting entities of type: {}", entityClass.getSimpleName());
        long startTime = System.currentTimeMillis();

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<T> root = cq.from(entityClass);
            cq.select(cb.count(root));

            Long count = entityManager.createQuery(cq).getSingleResult();
            long endTime = System.currentTimeMillis();
            logger.info("Count query executed in {}ms, found {} entities",
                    endTime - startTime, count);

            return count;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("Error counting entities of type: {} (execution time: {}ms)",
                    entityClass.getSimpleName(), endTime - startTime, e);
            throw new RuntimeException("Error counting entities", e);
        }
    }


    protected Optional<T> findSingleByJpql(String jpql, Object... parameters) {
        String logParams = formatParametersForLog(parameters);
        logger.debug("Executing JPQL single result query: {} with parameters: {}", jpql, logParams);
        long startTime = System.currentTimeMillis();

        try {
            TypedQuery<T> query = entityManager.createQuery(jpql, entityClass);
            applyParameters(query, parameters);

            try {
                T result = query.getSingleResult();
                long endTime = System.currentTimeMillis();
                logger.info("JPQL single result query executed in {}ms, result found",
                        endTime - startTime);
                return Optional.of(result);
            } catch (NoResultException e) {
                long endTime = System.currentTimeMillis();
                logger.info("JPQL single result query executed in {}ms, no result found",
                        endTime - startTime);
                return Optional.empty();
            }
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("Error executing JPQL single result query: {} with parameters: {} (execution time: {}ms)",
                    jpql, logParams, endTime - startTime, e);
            throw new RuntimeException("Error executing JPQL query", e);
        }
    }


    protected List<T> findByJpql(String jpql, Object... parameters) {
        String logParams = formatParametersForLog(parameters);
        logger.debug("Executing JPQL list query: {} with parameters: {}", jpql, logParams);
        long startTime = System.currentTimeMillis();

        try {
            TypedQuery<T> query = entityManager.createQuery(jpql, entityClass);
            applyParameters(query, parameters);

            List<T> results = query.getResultList();
            long endTime = System.currentTimeMillis();
            logger.info("JPQL list query executed in {}ms, found {} results",
                    endTime - startTime, results.size());

            return results;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("Error executing JPQL list query: {} with parameters: {} (execution time: {}ms)",
                    jpql, logParams, endTime - startTime, e);
            throw new RuntimeException("Error executing JPQL query", e);
        }
    }


    private void applyParameters(TypedQuery<T> query, Object... parameters) {
        for (int i = 0; i < parameters.length; i += 2) {
            if (i + 1 < parameters.length) {
                query.setParameter(parameters[i].toString(), parameters[i + 1]);
            }
        }
    }


    private String formatParametersForLog(Object... parameters) {
        if (parameters.length == 0) {
            return "none";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameters.length; i += 2) {
            if (i > 0) {
                sb.append(", ");
            }

            if (i + 1 < parameters.length) {
                Object value = parameters[i + 1];
                String valueStr = value == null ? "null" : value.toString();
                // Truncate very long values
                if (valueStr.length() > 100) {
                    valueStr = valueStr.substring(0, 97) + "...";
                }
                sb.append(parameters[i]).append("=").append(valueStr);
            }
        }

        return sb.toString();
    }

    public void bulkSave(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            logger.debug("Bulk save called with empty or null entity list");
            return;
        }

        logger.debug("Performing bulk save of {} entities of type: {}",
                entities.size(), entityClass.getSimpleName());
        long startTime = System.currentTimeMillis();

        boolean isActiveTransaction = entityManager.getTransaction().isActive();
        if (!isActiveTransaction) {
            logger.debug("Starting new transaction for bulk save");
            entityManager.getTransaction().begin();
        }

        try {
            int count = 0;
            int persisted = 0;
            int merged = 0;

            for (T entity : entities) {
                Object id = getEntityId(entity);
                if (id == null) {
                    entityManager.persist(entity);
                    persisted++;
                } else {
                    entityManager.merge(entity);
                    merged++;
                }

                if (++count % 50 == 0) {
                    logger.debug("Flushing and clearing persistence context after {} entities", count);
                    entityManager.flush();
                    entityManager.clear();
                }
            }

            if (!isActiveTransaction) {
                logger.debug("Committing transaction for bulk save");
                entityManager.getTransaction().commit();
            }

            long endTime = System.currentTimeMillis();
            logger.info("Bulk save completed in {}ms: {} entities processed ({} persisted, {} merged)",
                    endTime - startTime, entities.size(), persisted, merged);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("Error during bulk save of {} entities (execution time: {}ms)",
                    entities.size(), endTime - startTime, e);

            if (!isActiveTransaction) {
                logger.debug("Rolling back transaction after bulk save error");
                entityManager.getTransaction().rollback();
            }

            throw new RuntimeException("Error during bulk save operation", e);
        }
    }


    protected Object getEntityId(T entity) {
        if (entity == null) {
            return null;
        }

        try {

            try {
                var method = entityClass.getMethod("getId");
                return method.invoke(entity);
            } catch (NoSuchMethodException e) {

                var field = entityClass.getDeclaredField("id");
                field.setAccessible(true);
                return field.get(entity);
            }
        } catch (Exception e) {
            logger.error("Error getting ID from entity of type: {}", entityClass.getSimpleName(), e);
            throw new RuntimeException("Error getting entity ID", e);
        }
    }
}
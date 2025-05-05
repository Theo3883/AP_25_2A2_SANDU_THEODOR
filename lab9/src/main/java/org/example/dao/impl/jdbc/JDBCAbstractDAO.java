package org.example.dao.impl.jdbc;

import org.example.Database;
import org.example.dao.AbstractDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public abstract class JDBCAbstractDAO<T, ID> implements AbstractDAO<T, ID> {
    protected final Class<T> entityClass;
    protected final Logger logger;

    protected abstract String getCreateQuery();
    protected abstract void setCreateParameters(PreparedStatement statement, T entity) throws SQLException;

    protected abstract String getUpdateQuery();
    protected abstract void setUpdateParameters(PreparedStatement statement, T entity) throws SQLException;

    protected abstract String getFindByIdQuery();
    protected abstract void setFindByIdParameters(PreparedStatement statement, ID id) throws SQLException;

    protected abstract String getFindAllQuery();

    protected abstract String getDeleteQuery();
    protected abstract void setDeleteParameters(PreparedStatement statement, ID id) throws SQLException;

    protected abstract T mapRow(ResultSet rs) throws SQLException;
    protected abstract ID getEntityId(T entity);

    @SuppressWarnings("unchecked")
    public JDBCAbstractDAO() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        this.logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    public void create(T entity) {
        logger.debug("Creating new entity of type: {}", entityClass.getSimpleName());
        long startTime = System.currentTimeMillis();

        try (Connection conn = Database.getConnection();
             PreparedStatement statement = conn.prepareStatement(getCreateQuery())) {

            setCreateParameters(statement, entity);
            statement.executeUpdate();

            long endTime = System.currentTimeMillis();
            logger.info("Entity created in {}ms", endTime - startTime);
        } catch (SQLException e) {
            logger.error("Error creating entity: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating entity", e);
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        logger.debug("Finding {} with ID: {}", entityClass.getSimpleName(), id);
        long startTime = System.currentTimeMillis();

        try (Connection conn = Database.getConnection()) {
            assert conn != null;
            try (PreparedStatement statement = conn.prepareStatement(getFindByIdQuery())) {

                setFindByIdParameters(statement, id);

                try (ResultSet rs = statement.executeQuery()) {
                    long endTime = System.currentTimeMillis();
                    logger.info("FindById query executed in {}ms", endTime - startTime);

                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding entity by ID: {}", e.getMessage(), e);
            throw new RuntimeException("Error finding entity by ID", e);
        }
    }

    @Override
    public List<T> findAll() {
        logger.debug("Finding all entities of type: {}", entityClass.getSimpleName());
        long startTime = System.currentTimeMillis();

        try (Connection conn = Database.getConnection()) {
            assert conn != null;
            try (PreparedStatement statement = conn.prepareStatement(getFindAllQuery());
                 ResultSet rs = statement.executeQuery()) {

                List<T> results = mapResultSetToList(rs);

                long endTime = System.currentTimeMillis();
                logger.info("FindAll query executed in {}ms, found {} entities",
                        endTime - startTime, results.size());

                return results;
            }
        } catch (SQLException e) {
            logger.error("Error finding all entities: {}", e.getMessage(), e);
            throw new RuntimeException("Error finding all entities", e);
        }
    }

    @Override
    public void update(T entity) {
        logger.debug("Updating entity of type: {}", entityClass.getSimpleName());
        long startTime = System.currentTimeMillis();

        try (Connection conn = Database.getConnection()) {
            assert conn != null;
            try (PreparedStatement statement = conn.prepareStatement(getUpdateQuery())) {

                setUpdateParameters(statement, entity);
                statement.executeUpdate();

                long endTime = System.currentTimeMillis();
                logger.info("Entity updated in {}ms", endTime - startTime);

            }
        } catch (SQLException e) {
            logger.error("Error updating entity: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating entity", e);
        }
    }

    @Override
    public void delete(T entity) {
        deleteById(getEntityId(entity));
    }

    @Override
    public void deleteById(ID id) {
        logger.debug("Deleting {} with ID: {}", entityClass.getSimpleName(), id);
        long startTime = System.currentTimeMillis();

        try (Connection conn = Database.getConnection()) {
            assert conn != null;
            try (PreparedStatement statement = conn.prepareStatement(getDeleteQuery())) {

                setDeleteParameters(statement, id);
                int affectedRows = statement.executeUpdate();

                long endTime = System.currentTimeMillis();
                logger.info("Entity deletion completed in {}ms, affected rows: {}",
                        endTime - startTime, affectedRows);
            }
        } catch (SQLException e) {
            logger.error("Error deleting entity: {}", e.getMessage(), e);
            throw new RuntimeException("Error deleting entity", e);
        }
    }

    protected List<T> mapResultSetToList(ResultSet rs) throws SQLException {
        List<T> results = new java.util.ArrayList<>();
        while (rs.next()) {
            results.add(mapRow(rs));
        }
        return results;
    }
}
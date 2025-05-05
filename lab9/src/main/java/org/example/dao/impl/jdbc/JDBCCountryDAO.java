package org.example.dao.impl.jdbc;

import org.example.Database;
import org.example.dao.CountryDAO;
import org.example.model.Continent;
import org.example.model.Country;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JDBCCountryDAO extends JDBCAbstractDAO<Country, Integer> implements CountryDAO {

    @Override
    protected String getCreateQuery() {
        return "INSERT INTO countries (name, code, continent_id) VALUES (?, ?, ?)";
    }

    @Override
    protected void setCreateParameters(PreparedStatement statement, Country entity) throws SQLException {
        statement.setString(1, entity.getName());
        statement.setString(2, entity.getCode());
        statement.setInt(3, entity.getContinent().getId());
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE countries SET name = ?, code = ?, continent_id = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, Country entity) throws SQLException {
        statement.setString(1, entity.getName());
        statement.setString(2, entity.getCode());
        statement.setInt(3, entity.getContinent().getId());
        statement.setInt(4, entity.getId());
    }

    @Override
    protected String getFindByIdQuery() {
        return "SELECT * FROM countries WHERE id = ?";
    }

    @Override
    protected void setFindByIdParameters(PreparedStatement statement, Integer id) throws SQLException {
        statement.setInt(1, id);
    }

    @Override
    protected String getFindAllQuery() {
        return "SELECT * FROM countries";
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM countries WHERE id = ?";
    }

    @Override
    protected void setDeleteParameters(PreparedStatement statement, Integer id) throws SQLException {
        statement.setInt(1, id);
    }

    @Override
    protected Country mapRow(ResultSet rs) throws SQLException {
        Continent continent = new Continent();
        continent.setId(rs.getInt("continent_id"));

        return new Country(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("code"),
                continent,
                null
        );
    }

    @Override
    protected Integer getEntityId(Country entity) {
        return entity.getId();
    }

    @Override
    public List<Country> findByName(String namePattern) {
        String query = "SELECT * FROM countries WHERE name LIKE ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, namePattern);
            ResultSet rs = stmt.executeQuery();
            return mapResultSetToList(rs);

        } catch (SQLException e) {
            logger.error("Error finding countries by name pattern: {}", namePattern, e);
            throw new RuntimeException("Error finding countries by name pattern", e);
        }
    }

    @Override
    public List<Country> findByContinent(Integer continentId) {
        String query = "SELECT * FROM countries WHERE continent_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, continentId);
            ResultSet rs = stmt.executeQuery();
            return mapResultSetToList(rs);

        } catch (SQLException e) {
            logger.error("Error finding countries by continent ID: {}", continentId, e);
            throw new RuntimeException("Error finding countries by continent ID", e);
        }
    }

    @Override
    public Country findByCode(String code) {
        String query = "SELECT * FROM countries WHERE code = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            } else {
                return null;
            }

        } catch (SQLException e) {
            logger.error("Error finding country by code: {}", code, e);
            throw new RuntimeException("Error finding country by code", e);
        }
    }

    @Override
    protected List<Country> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Country> countries = new java.util.ArrayList<>();
        while (rs.next()) {
            countries.add(mapRow(rs));
        }
        return countries;
    }
}

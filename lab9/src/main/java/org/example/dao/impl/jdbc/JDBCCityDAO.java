package org.example.dao.impl.jdbc;

import org.example.Database;
import org.example.dao.CityDAO;
import org.example.model.City;
import org.example.model.Country;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JDBCCityDAO extends JDBCAbstractDAO<City, Integer> implements CityDAO {
    @Override
    protected String getCreateQuery() {
        return "INSERT INTO cities (country_id, name, is_capital, latitude, longitude, population) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setCreateParameters(PreparedStatement statement, City entity) throws SQLException {
        statement.setInt(1, entity.getCountry().getId());
        statement.setString(2, entity.getName());
        statement.setObject(3, entity.getIsCapital());
        statement.setObject(4, entity.getLatitude());
        statement.setObject(5, entity.getLongitude());
        statement.setObject(6, entity.getPopulation());
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE cities SET country_id = ?, name = ?, is_capital = ?, latitude = ?, longitude = ?, population = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, City entity) throws SQLException {
        statement.setInt(1, entity.getCountry().getId());
        statement.setString(2, entity.getName());
        statement.setObject(3, entity.getIsCapital());
        statement.setObject(4, entity.getLatitude());
        statement.setObject(5, entity.getLongitude());
        statement.setObject(6, entity.getPopulation());
        statement.setInt(7, entity.getId());
    }

    @Override
    protected String getFindByIdQuery() {
        return "SELECT * FROM cities WHERE id = ?";
    }

    @Override
    protected void setFindByIdParameters(PreparedStatement statement, Integer id) throws SQLException {
        statement.setInt(1, id);
    }

    @Override
    protected String getFindAllQuery() {
        return "SELECT * FROM cities";
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM cities WHERE id = ?";
    }

    @Override
    protected void setDeleteParameters(PreparedStatement statement, Integer id) throws SQLException {
        statement.setInt(1, id);
    }

    @Override
    protected City mapRow(ResultSet rs) throws SQLException {
        Country country = new Country();
        country.setId(rs.getInt("country_id"));

        return new City(
                rs.getInt("id"),
                country,
                rs.getString("name"),
                (Boolean) rs.getObject("is_capital"),
                (Double) rs.getObject("latitude"),
                (Double) rs.getObject("longitude"),
                (Integer) rs.getObject("population"),
                null,
                null
        );
    }

    @Override
    protected Integer getEntityId(City entity) {
        return entity.getId();
    }

    @Override
    public List<City> findByName(String namePattern) {
        String query = "SELECT * FROM cities WHERE name LIKE ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, namePattern);
            ResultSet rs = stmt.executeQuery();
            return mapResultSetToList(rs);

        } catch (SQLException e) {
            logger.error("Error finding cities by name pattern: {}", namePattern, e);
            throw new RuntimeException("Error finding cities by name pattern", e);
        }
    }

    @Override
    public List<City> findByCountry(Integer countryId) {
        String query = "SELECT * FROM cities WHERE country_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, countryId);
            ResultSet rs = stmt.executeQuery();
            return mapResultSetToList(rs);

        } catch (SQLException e) {
            logger.error("Error finding cities by country ID: {}", countryId, e);
            throw new RuntimeException("Error finding cities by country ID", e);
        }
    }

    @Override
    public List<City> findCapitals() {
        String query = "SELECT * FROM cities WHERE is_capital = true";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            return mapResultSetToList(rs);

        } catch (SQLException e) {
            logger.error("Error finding capital cities", e);
            throw new RuntimeException("Error finding capital cities", e);
        }
    }

    @Override
    public List<City> findByCoordinatesRange(Double minLat, Double maxLat, Double minLong, Double maxLong) {
        String query = "SELECT * FROM cities WHERE latitude BETWEEN ? AND ? AND longitude BETWEEN ? AND ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDouble(1, minLat);
            stmt.setDouble(2, maxLat);
            stmt.setDouble(3, minLong);
            stmt.setDouble(4, maxLong);
            ResultSet rs = stmt.executeQuery();
            return mapResultSetToList(rs);

        } catch (SQLException e) {
            logger.error("Error finding cities by coordinates range", e);
            throw new RuntimeException("Error finding cities by coordinates range", e);
        }
    }

    @Override
    public List<City> findByFirstLetter(char letter) {
        String query = "SELECT * FROM cities WHERE name LIKE ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, letter + "%");
            ResultSet rs = stmt.executeQuery();
            return mapResultSetToList(rs);

        } catch (SQLException e) {
            logger.error("Error finding cities by first letter: {}", letter, e);
            throw new RuntimeException("Error finding cities by first letter", e);
        }
    }

    @Override
    public List<City> findByPopulationRange(Integer min, Integer max) {
        String query = "SELECT * FROM cities WHERE population BETWEEN ? AND ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, min);
            stmt.setInt(2, max);
            ResultSet rs = stmt.executeQuery();
            return mapResultSetToList(rs);

        } catch (SQLException e) {
            logger.error("Error finding cities by population range", e);
            throw new RuntimeException("Error finding cities by population range", e);
        }
    }

    @Override
    protected List<City> mapResultSetToList(ResultSet rs) throws SQLException {
        List<City> cities = new ArrayList<>();
        while (rs.next()) {
            cities.add(mapRow(rs));
        }
        return cities;
    }
}

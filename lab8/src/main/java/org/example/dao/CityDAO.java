package org.example.dao;

import org.example.model.City;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CityDAO extends AbstractDAO<City> {
    public CityDAO(Connection con) {
        this.con = con;
    }

    @Override
    protected String getTableName() {
        return "cities";
    }

    @Override
    protected City mapResultSetToEntity(ResultSet rs) throws SQLException {
        City city = new City();
        city.setId(rs.getInt("id"));
        city.setName(rs.getString("name"));
        city.setCountryId(rs.getInt("country_id"));
        city.setCapital(rs.getBoolean("capital"));
        city.setLatitude(rs.getDouble("latitude"));
        city.setLongitude(rs.getDouble("longitude"));
        return city;
    }

    @Override
    public void create(City city) throws SQLException {
        String sql = "INSERT INTO cities (country_id, name, is_capital, latitude, longitude) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, city.getCountryId());
            stmt.setString(2, city.getName());
            stmt.setBoolean(3, city.isCapital());
            stmt.setDouble(4, city.getLatitude());
            stmt.setDouble(5, city.getLongitude());
            stmt.executeUpdate();
        }
    }

    @Override
    public List<City> findAll() throws SQLException {
        List<City> cities = new ArrayList<>();
        String sql = "SELECT * FROM cities";
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                City city = new City();
                city.setId(rs.getInt("id"));
                city.setCountryId(rs.getInt("country_id"));
                city.setName(rs.getString("name"));
                city.setCapital(rs.getBoolean("is_capital"));
                city.setLatitude(rs.getDouble("latitude"));
                city.setLongitude(rs.getDouble("longitude"));
                cities.add(city);
            }
        }
        return cities;
    }

    @Override
    public City findById(int id) throws SQLException {
        String sql = "SELECT * FROM cities WHERE id = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    City city = new City();
                    city.setId(rs.getInt("id"));
                    city.setCountryId(rs.getInt("country_id"));
                    city.setName(rs.getString("name"));
                    city.setCapital(rs.getBoolean("is_capital"));
                    city.setLatitude(rs.getDouble("latitude"));
                    city.setLongitude(rs.getDouble("longitude"));
                    return city;
                }
            }
        }
        return null;
    }
}

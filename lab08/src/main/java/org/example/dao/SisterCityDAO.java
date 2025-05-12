package org.example.dao;

import org.example.model.SisterCity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SisterCityDAO extends AbstractDAO<SisterCity> {
    public SisterCityDAO(Connection con) {
        this.con = con;
    }

    @Override
    protected String getTableName() {
        return "sister_cities";
    }

    @Override
    protected SisterCity mapResultSetToEntity(ResultSet rs) throws SQLException {
        SisterCity sisterCity = new SisterCity();
        sisterCity.setId(rs.getInt("id"));
        sisterCity.setCity1Id(rs.getInt("city1_id"));
        sisterCity.setCity2Id(rs.getInt("city2_id"));
        return sisterCity;
    }

    @Override
    public void create(SisterCity sisterCity) throws SQLException {
        String sql = "INSERT INTO sister_cities (city1_id, city2_id) VALUES (?, ?) RETURNING id";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, sisterCity.getCity1Id());
            stmt.setInt(2, sisterCity.getCity2Id());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    sisterCity.setId(rs.getInt("id"));
                }
            }
        }
    }

    public List<SisterCity> findByCityId(int cityId) throws SQLException {
        List<SisterCity> sisterCities = new ArrayList<>();
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT * FROM sister_cities WHERE city1_id = ? OR city2_id = ?")) {
            stmt.setInt(1, cityId);
            stmt.setInt(2, cityId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sisterCities.add(mapResultSetToEntity(rs));
                }
            }
        }
        return sisterCities;
    }

    public void createBatch(List<SisterCity> sisterCities) throws SQLException {
        String sql = "INSERT INTO sister_cities (city1_id, city2_id) VALUES (?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            con.setAutoCommit(false);
            for (SisterCity sisterCity : sisterCities) {
                stmt.setInt(1, sisterCity.getCity1Id());
                stmt.setInt(2, sisterCity.getCity2Id());
                stmt.addBatch();
            }
            stmt.executeBatch();
            con.commit();
            con.setAutoCommit(true);
        }
    }
}
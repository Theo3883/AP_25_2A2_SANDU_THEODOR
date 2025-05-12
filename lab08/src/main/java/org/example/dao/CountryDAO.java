package org.example.dao;

import org.example.model.Country;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CountryDAO extends AbstractDAO<Country> {
    public CountryDAO(Connection con) {
        this.con = con;
    }

    @Override
    protected String getTableName() {
        return "countries";
    }

    @Override
    protected Country mapResultSetToEntity(ResultSet rs) throws SQLException {
        Country country = new Country();
        country.setId(rs.getInt("id"));
        country.setName(rs.getString("name"));
        country.setCode(rs.getString("code"));
        country.setContinentId(rs.getInt("continent_id"));
        return country;
    }

    @Override
    public void create(Country country) throws SQLException {
        String sql = "insert into countries (name, code, continent_id) values (?, ?, ?) returning id";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, country.getName());
            stmt.setString(2, country.getCode());
            stmt.setInt(3, country.getContinentId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    country.setId(rs.getInt("id"));
                }
            }
        }
    }

    public List<Country> findByContinentId(int continentId) throws SQLException {
        List<Country> countries = new ArrayList<>();
        try (PreparedStatement stmt = con.prepareStatement(
                "select * from countries where continent_id = ?")) {
            stmt.setInt(1, continentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    countries.add(mapResultSetToEntity(rs));
                }
            }
        }
        return countries;
    }
}

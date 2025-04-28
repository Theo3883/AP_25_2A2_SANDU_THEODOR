package org.example.dao;

import org.example.model.Continent;

import java.sql.*;

public class ContinentDAO extends AbstractDAO<Continent> {
    public ContinentDAO(Connection con) {
        this.con = con;
    }
    @Override
    protected String getTableName() { return "continents"; }
    @Override
    protected Continent mapResultSetToEntity(ResultSet rs) throws SQLException {
        Continent continent = new Continent();
        continent.setId(rs.getInt("id"));
        continent.setName(rs.getString("name"));
        return continent;
    }
    @Override
    public void create(Continent continent) throws SQLException {
        String sql = "INSERT INTO continents (name) VALUES (?) RETURNING id";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, continent.getName());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    continent.setId(rs.getInt("id"));
                }
            }
        }
    }
}


package org.example.dao;

import org.example.Database;
import org.example.model.Model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDAO<T extends Model> {
    protected Connection con = Database.getConnection();

    protected abstract String getTableName();

    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;

    public void create(T entity) throws SQLException {}

    public T findById(int id) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement("select * from " + getTableName() + " where id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    public List<T> findAll() throws SQLException {
        List<T> entities = new ArrayList<>();
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("select * from " + getTableName())) {
            while (rs.next()) {
                entities.add(mapResultSetToEntity(rs));
            }
        }
        return entities;
    }

    public List<T> findByName(String name) throws SQLException {
        List<T> entities = new ArrayList<>();
        try (PreparedStatement stmt = con.prepareStatement("select * from " + getTableName() + " where name = ?")) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entities.add(mapResultSetToEntity(rs));
                }
            }
        }
        return entities;
    }
}


package org.example.dao;

import java.util.List;
import java.util.Optional;

public interface AbstractDAO<T, ID> {
    void create(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void update(T entity);
    void delete(T entity);
    void deleteById(ID id);
}
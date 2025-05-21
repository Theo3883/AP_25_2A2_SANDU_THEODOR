package com.backend.demo.repository;

import com.backend.demo.model.Continent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContinentRepository extends JpaRepository<Continent, Integer> {
    Optional<Continent> findByName(String name);
    boolean existsByName(String name);
} 
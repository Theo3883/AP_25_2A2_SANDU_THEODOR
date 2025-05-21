package com.backend.demo.repository;

import com.backend.demo.model.Continent;
import com.backend.demo.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {
    Optional<Country> findByName(String name);
    List<Country> findByContinent(Continent continent);
    boolean existsByName(String name);
    boolean existsByCode(String code);
    
    @Query("SELECT c FROM Country c JOIN FETCH c.continent")
    List<Country> findAllWithContinent();
} 
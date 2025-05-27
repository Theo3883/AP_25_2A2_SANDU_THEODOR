package com.backend.demo.repository;

import com.backend.demo.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {

    Optional<Country> findByName(String name);

    @Query("SELECT c FROM Country c JOIN FETCH c.continent")
    List<Country> findAllWithContinent();
    
    @Query("SELECT DISTINCT c FROM Country c LEFT JOIN FETCH c.neighbors")
    List<Country> findAllWithNeighbors();
    
    @Query("SELECT DISTINCT c FROM Country c JOIN FETCH c.continent LEFT JOIN FETCH c.neighbors")
    List<Country> findAllWithContinentAndNeighbors();
    
    @Modifying
    @Query("UPDATE Country c SET c.color = null")
    void resetAllColors();
}
package com.backend.demo.repository;

import com.backend.demo.model.City;
import com.backend.demo.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {
    List<City> findByCountry(Country country);
    Optional<City> findByNameAndCountry(String name, Country country);
    List<City> findByIsCapital(Boolean isCapital);
    
    @Query("SELECT c FROM City c JOIN FETCH c.country WHERE c.id = :id")
    Optional<City> findByIdWithCountry(@Param("id") Integer id);
    
    @Query("SELECT c FROM City c JOIN FETCH c.country")
    List<City> findAllWithCountry();
    
    @Query("SELECT c FROM City c WHERE c.latitude BETWEEN :minLat AND :maxLat AND c.longitude BETWEEN :minLon AND :maxLon")
    List<City> findCitiesInRegion(
            @Param("minLat") Double minLat, 
            @Param("maxLat") Double maxLat, 
            @Param("minLon") Double minLon, 
            @Param("maxLon") Double maxLon);
} 
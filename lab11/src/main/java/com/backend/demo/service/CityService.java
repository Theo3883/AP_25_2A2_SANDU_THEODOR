package com.backend.demo.service;

import com.backend.demo.dto.CityDTO;
import com.backend.demo.model.City;
import com.backend.demo.model.Country;
import com.backend.demo.repository.CityRepository;
import com.backend.demo.repository.CountryRepository;
import com.backend.demo.utils.CityGenerator;
import com.backend.demo.utils.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class CityService {
    
    private static final Logger logger = LoggerFactory.getLogger(CityService.class);
    
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final CityGenerator cityGenerator;
    private final Mapper mapper;
    private final Random random = new Random();
    
    @Autowired
    public CityService(CityRepository cityRepository, CountryRepository countryRepository, 
                       CityGenerator cityGenerator, Mapper mapper) {
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
        this.cityGenerator = cityGenerator;
        this.mapper = mapper;
    }

    public List<CityDTO> getAllCities() {
        List<City> cities = cityRepository.findAll();
        return mapper.toCityDTOs(cities);
    }

    @Transactional
    public List<CityDTO> generateCities(int count) {
        logger.info("Clearing existing cities from the database before generation");
        
        // Delete all cities directly
        cityRepository.deleteAll();
        logger.info("Successfully cleared all existing cities");
        
        // Now generate new cities
        logger.info("Starting city generation process for {} cities", count);
        List<City> generatedCities = cityGenerator.generateCities(count);
        logger.info("Completed city generation, created {} cities", generatedCities.size());
        return mapper.toCityDTOs(generatedCities);
    }

    @Transactional
    public CityDTO createCity(CityDTO cityDTO) {
        Optional<Country> country = countryRepository.findById(cityDTO.getCountryId());
        
        if (country.isEmpty()) {
            return null;
        }
        
        City city = new City();
        city.setName(cityDTO.getName());
        city.setCountry(country.get());
        city.setIsCapital(cityDTO.getIsCapital());
        city.setLatitude(cityDTO.getLatitude());
        city.setLongitude(cityDTO.getLongitude());
        city.setPopulation(cityDTO.getPopulation());
        
        City savedCity = cityRepository.save(city);
        return mapper.toCityDTO(savedCity);
    }

    @Transactional
    public CityDTO updateCityName(Integer id, String name) {
        Optional<City> existingCity = cityRepository.findById(id);
        
        if (existingCity.isEmpty()) {
            return null;
        }
        
        City city = existingCity.get();
        city.setName(name);
        
        City updatedCity = cityRepository.save(city);
        return mapper.toCityDTO(updatedCity);
    }

    @Transactional
    public boolean deleteCity(Integer id) {
        if (cityRepository.existsById(id)) {
            cityRepository.deleteById(id);
            return true;
        }
        return false;
    }
} 
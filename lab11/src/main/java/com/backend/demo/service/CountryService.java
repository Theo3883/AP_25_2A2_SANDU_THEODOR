package com.backend.demo.service;

import com.backend.demo.model.Country;
import com.backend.demo.repository.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CountryService {
    
    private final CountryRepository countryRepository;
    
    @Autowired
    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }
    
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }
    
    public List<Country> getAllCountriesWithNeighbors() {
        return countryRepository.findAllWithNeighbors();
    }
    
    public List<Country> getAllCountriesWithContinentAndNeighbors() {
        return countryRepository.findAllWithContinentAndNeighbors();
    }
    
    @Transactional
    public void resetAllColors() {
        countryRepository.resetAllColors();
    }
}

package com.backend.demo.service;

import com.backend.demo.dto.ContinentDTO;
import com.backend.demo.model.Continent;
import com.backend.demo.repository.ContinentRepository;
import com.backend.demo.utils.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContinentService {
    
    private final ContinentRepository continentRepository;
    private final Mapper mapper;
    
    @Autowired
    public ContinentService(ContinentRepository continentRepository, Mapper mapper) {
        this.continentRepository = continentRepository;
        this.mapper = mapper;
    }

    public List<ContinentDTO> getAllContinents() {
        List<Continent> continents = continentRepository.findAll();
        return mapper.toContinentDTOs(continents);
    }

    public ContinentDTO createContinent(ContinentDTO continentDTO) {
        if (continentDTO.getName() == null || continentDTO.getName().trim().isEmpty()) {
            return null;
        }

        if (continentRepository.existsByName(continentDTO.getName())) {
            return null;
        }
        
        Continent continent = new Continent();
        continent.setName(continentDTO.getName());
        
        Continent savedContinent = continentRepository.save(continent);
        return mapper.toContinentDTO(savedContinent);
    }

    public ContinentDTO updateContinentName(Integer id, String name) {
        Optional<Continent> existingContinent = continentRepository.findById(id);
        
        if (existingContinent.isPresent()) {
            Continent continent = existingContinent.get();
            continent.setName(name);
            
            Continent updatedContinent = continentRepository.save(continent);
            return mapper.toContinentDTO(updatedContinent);
        }
        
        return null;
    }

    public boolean deleteContinent(Integer id) {
        if (continentRepository.existsById(id)) {
            continentRepository.deleteById(id);
            return true;
        }
        return false;
    }
} 
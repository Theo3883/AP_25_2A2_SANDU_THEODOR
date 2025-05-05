package org.example.utils;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.example.dao.CityDAO;
import org.example.factory.DAOFactory;
import org.example.model.City;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class CitySolver {
    private static final Logger logger = LoggerFactory.getLogger(CitySolver.class);

    public List<City> findCitiesByConstraints(char firstLetter, int minPopulation, int maxPopulation) {
        logger.info("Finding cities starting with '{}' with population between {} and {}",
                firstLetter, minPopulation, maxPopulation);

        CityDAO cityDAO = DAOFactory.getCityDAO();
        List<City> citiesByLetter = cityDAO.findByFirstLetter(firstLetter);

        if (citiesByLetter.isEmpty()) {
            logger.info("No cities found starting with '{}'", firstLetter);
            return Collections.emptyList();
        }

        Map<Integer, List<City>> citiesByCountry = citiesByLetter.stream()
                .collect(Collectors.groupingBy(city -> city.getCountry().getId()));

        Model model = new Model("City Selection Problem");

        List<City> allCities = new ArrayList<>(citiesByLetter);
        BoolVar[] selected = model.boolVarArray("selected", allCities.size());

        int[] populations = allCities.stream()
                .mapToInt(c -> c.getPopulation() != null ? c.getPopulation() : 0)
                .toArray();

        IntVar totalPopulation = model.intVar("totalPopulation", minPopulation, maxPopulation);

        model.scalar(selected, populations, "=", totalPopulation).post();

        for (List<City> countryCities : citiesByCountry.values()) {
            if (countryCities.size() > 1) {
                List<Integer> indices = new ArrayList<>();
                for (City city : countryCities) {
                    indices.add(allCities.indexOf(city));
                }

                BoolVar[] countryVars = new BoolVar[indices.size()];
                for (int i = 0; i < indices.size(); i++) {
                    countryVars[i] = selected[indices.get(i)];
                }
                model.sum(countryVars, "<=", 1).post();
            }
        }

        Solver solver = model.getSolver();
        Solution solution = solver.findSolution();

        if (solution != null) {
            List<City> selectedCities = new ArrayList<>();
            for (int i = 0; i < selected.length; i++) {
                if (solution.getIntVal(selected[i]) == 1) {
                    selectedCities.add(allCities.get(i));
                }
            }

            int totalPop = selectedCities.stream()
                    .mapToInt(c -> c.getPopulation() != null ? c.getPopulation() : 0)
                    .sum();

            logger.info("Found solution with {} cities, total population: {}",
                    selectedCities.size(), totalPop);

            return selectedCities;
        } else {
            logger.info("No solution found for the given constraints");
            return Collections.emptyList();
        }
    }
}
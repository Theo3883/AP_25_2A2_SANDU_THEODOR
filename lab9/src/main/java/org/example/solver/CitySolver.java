package org.example.solver;

import org.example.dao.CityDAO;
import org.example.factory.DAOFactory;
import org.example.model.City;
import org.example.solver.domain.CitySelectionSolution;
import org.example.solver.domain.PlanningCity;
import org.example.solver.score.CityConstraintProvider;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
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

        // Convert City objects to PlanningCity objects for OptaPlanner
        List<PlanningCity> planningCities = citiesByLetter.stream()
                .map(city -> new PlanningCity(
                        city.getId(),
                        city.getName(),
                        city.getPopulation() != null ? city.getPopulation() : 0,
                        city.getCountry().getId(),
                        city.getCountry().getName()))
                .collect(Collectors.toList());

        CityConstraintProvider.PopulationRangeHolder.setPopulationRange(minPopulation, maxPopulation);

        CitySelectionSolution problem = new CitySelectionSolution();
        problem.setCities(planningCities);
        problem.setMinPopulation(minPopulation);
        problem.setMaxPopulation(maxPopulation);

        SolverFactory<CitySelectionSolution> solverFactory = SolverFactory.createFromXmlResource(
                "citySolverConfig.xml");
        Solver<CitySelectionSolution> solver = solverFactory.buildSolver();

        CitySelectionSolution solution = solver.solve(problem);

        List<City> selectedCities = solution.getCities().stream()
                .filter(pc -> Boolean.TRUE.equals(pc.getSelected()))
                .map(planningCity -> citiesByLetter.stream()
                        .filter(city -> city.getId().equals(planningCity.getId()))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        int totalPopulation = selectedCities.stream()
                .mapToInt(city -> city.getPopulation() != null ? city.getPopulation() : 0)
                .sum();

        logger.info("Found solution with {} cities, total population: {}",
                selectedCities.size(), totalPopulation);

        return selectedCities;
    }
}

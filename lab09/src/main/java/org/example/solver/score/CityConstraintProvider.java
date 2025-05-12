package org.example.solver.score;

import lombok.Getter;
import org.example.solver.domain.PlanningCity;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

public class CityConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            uniqueCountryConstraint(constraintFactory),
            populationRangeConstraint(constraintFactory),

            maximizeCitiesConstraint(constraintFactory)
        };
    }

    private Constraint uniqueCountryConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory
            .forEachUniquePair(PlanningCity.class,
                Joiners.equal(PlanningCity::getCountryId),
                Joiners.filtering((city1, city2) -> 
                    Boolean.TRUE.equals(city1.getSelected()) &&
                    Boolean.TRUE.equals(city2.getSelected())))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Unique Country Constraint");
    }

    private Constraint populationRangeConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(PlanningCity.class)
                .filter(city -> Boolean.TRUE.equals(city.getSelected()))
                .penalize(HardSoftScore.ONE_HARD,
                        city -> {
                            int population = city.getPopulation();
                            int minPopulation = PopulationRangeHolder.getMinPopulation();
                            int maxPopulation = PopulationRangeHolder.getMaxPopulation();

                            int penalty = 0;
                            if (population < minPopulation) {
                                penalty += minPopulation - population;
                            }
                            if (population > maxPopulation) {
                                penalty += population - maxPopulation;
                            }

                            return penalty;
                        })
                .asConstraint("Population range constraint");
    }


    private Constraint maximizeCitiesConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory
            .forEach(PlanningCity.class)
            .filter(city -> Boolean.TRUE.equals(city.getSelected()))
            .reward(HardSoftScore.ONE_SOFT)
            .asConstraint("Maximize Selected Cities");
    }

    public static class PopulationRangeHolder {
        @Getter
        private static int minPopulation;
        @Getter
        private static int maxPopulation;

        public static void setPopulationRange(int min, int max) {
            minPopulation = min;
            maxPopulation = max;
        }
    }
}

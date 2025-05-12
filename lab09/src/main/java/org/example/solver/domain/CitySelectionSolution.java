package org.example.solver.domain;

import org.optaplanner.core.api.domain.solution.*;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@PlanningSolution
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitySelectionSolution {

    @PlanningEntityCollectionProperty
    private List<PlanningCity> cities;

    private int minPopulation;

    private int maxPopulation;

    @PlanningScore
    private HardSoftScore score;

    @ValueRangeProvider(id = "selectionRange")
    @ProblemFactCollectionProperty
    public List<Boolean> getSelectionRange() {
        return Arrays.asList(Boolean.TRUE, Boolean.FALSE);
    }

    @ProblemFactProperty
    @ValueRangeProvider(id = "minPopulation")
    public int getMinPopulation() {
        return minPopulation;
    }

    @ProblemFactProperty
    @ValueRangeProvider(id = "maxPopulation")
    public int getMaxPopulation() {
        return maxPopulation;
    }
}

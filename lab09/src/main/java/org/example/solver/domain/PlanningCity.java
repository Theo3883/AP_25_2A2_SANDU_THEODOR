package org.example.solver.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
@Data
@NoArgsConstructor
public class PlanningCity {
    @PlanningId
    private Integer id;
    private String name;
    private int population;
    private Integer countryId;
    private String countryName;

    private Boolean selected;

    public PlanningCity(Integer id, String name, int population, Integer countryId, String countryName) {
        this.id = id;
        this.name = name;
        this.population = population;
        this.countryId = countryId;
        this.countryName = countryName;
    }

    @PlanningVariable(valueRangeProviderRefs = "selectionRange")
    public Boolean getSelected() {
        return selected;
    }

    @Override
    public String toString() {
        return name + " (pop: " + population + ", country: " + countryName + ")";
    }
}

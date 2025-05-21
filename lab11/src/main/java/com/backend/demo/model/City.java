package com.backend.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class City extends Model {
    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "is_capital")
    private Boolean isCapital;

    private Double latitude;
    private Double longitude;

    @Column(name = "population")
    private Integer population;
} 
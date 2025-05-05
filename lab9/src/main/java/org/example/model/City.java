package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "cities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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

    @OneToMany(mappedBy = "city1")
    private List<SisterCity> sisterCitiesAsCity1;

    @OneToMany(mappedBy = "city2")
    private List<SisterCity> sisterCitiesAsCity2;
}
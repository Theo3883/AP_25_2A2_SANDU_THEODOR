package com.backend.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "continents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Continent extends Model {
    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "continent", cascade = CascadeType.ALL)
    private List<Country> countries;

} 
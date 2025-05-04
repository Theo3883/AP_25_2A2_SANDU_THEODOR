package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sister_cities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SisterCity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "city1_id")
    private City city1;

    @ManyToOne
    @JoinColumn(name = "city2_id")
    private City city2;
}
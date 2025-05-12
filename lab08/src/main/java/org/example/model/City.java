package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class City extends Model{
    private int id;
    private int countryId;
    private String name;
    private boolean isCapital;
    private double latitude;
    private double longitude;
}

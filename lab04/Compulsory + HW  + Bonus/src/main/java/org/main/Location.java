package org.main;

import lombok.*;

@AllArgsConstructor
@Getter
public class Location implements Comparable<Location> {
    String name;
    Type type;

    @Override
    public int compareTo(Location other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return String.format("Location: %s, Type: %s", name, type);
    }
}
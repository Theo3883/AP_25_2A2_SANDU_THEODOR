package org.main;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
public class LocationPair {
    private Location from;
    private Location to;
    private boolean canMoveDirectly;
    private double timeToTravel;
    private double probabilityToReachSafely;

    @Override
    public String toString() {
        return String.format("From: %s, To: %s, Can Move Directly: %s, Time to Travel: %.2f, Probability to Reach Safely: %.2f",
                from.getName(), to.getName(), canMoveDirectly, timeToTravel, probabilityToReachSafely);
    }
}

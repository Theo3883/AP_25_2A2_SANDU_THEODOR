package org.example;

public class Freighter extends Aircraft implements CargoCapable,PassengerCapable {

    public final int maximumPayload;
    private final int  wingSpan;

    public Freighter(String name, String model, int tailNumber,int maxPayload, int wingSpan) {
        super(name, model, tailNumber);
        this.maximumPayload = maxPayload;
        this.wingSpan = wingSpan;
    }

    public int getWingSpan() {
        return this.wingSpan;
    }

    @Override
    public boolean isPassengerCapable() {
        return false;
    }

    @Override
    public int getPassengerCapacity() {
        return 0;
    }

    @Override
    public boolean isCargoCapable() {
        return true;
    }

    @Override
    public int getCargoCapacity() {
        return this.maximumPayload;
    }
}

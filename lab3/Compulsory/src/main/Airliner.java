package main;

public class Airliner extends Aircraft implements CargoCapable, PassengerCapable{
    private final int passengerCapacity;
    private final int  wingSpan;

    public Airliner(String name, String model, int tailNumber, int passengerCapacity, int wingSpan) {
        super(name, model, tailNumber);
        this.passengerCapacity = passengerCapacity;
        this.wingSpan = wingSpan;
    }

    public int getWingSpan() {
        return this.wingSpan;
    }

    @Override
    public boolean isPassengerCapable() {
        return true;
    }


    @Override
    public int getPassengerCapacity() {
        return this.passengerCapacity;
    }

    @Override
    public boolean isCargoCapable() {
        return false;
    }

    @Override
    public int getCargoCapacity() {
        return 0;
    }
}

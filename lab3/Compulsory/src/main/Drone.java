package main;

public class Drone extends Aircraft implements PassengerCapable,CargoCapable {

    private final int batteryLife;

    public Drone(String name, String model, int tailNumber,int batteryLife) {
        super(name, model, tailNumber);
        this.batteryLife = batteryLife;
    }

    public int getBatteryLife() {
        return this.batteryLife;
    }

    @Override
    public boolean isCargoCapable() {
        return false;
    }

    @Override
    public int getCargoCapacity() {
        return 0;
    }

    @Override
    public boolean isPassengerCapable() {
        return false;
    }

    @Override
    public int getPassengerCapacity() {
        return 0;
    }
}

package org.example;

abstract class Aircraft {
    String name;
    String model;
    int tailNumber;

    protected Aircraft(String name, String model, int tailNumber) {
        this.name = name;
        this.model = model;
        this.tailNumber = tailNumber;
    }
    public String getName() {
        return name;
    }
    public String getModel() {
        return model;
    }
    public int getTailNumber() {
        return tailNumber;
    }
}

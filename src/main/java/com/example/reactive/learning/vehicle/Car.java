package com.example.reactive.learning.vehicle;

import java.util.List;

public class Car {

    private Chassis chassis;
    private Engine engine;
    private List<Wheel> wheels;

    public Car() {

    }

    public Car(Chassis chassis, Engine engine, List<Wheel> wheels) {
        this.chassis = chassis;
        this.engine = engine;
        this.wheels = wheels;
    }

    public Chassis getChassis() {
        return chassis;
    }

    public void setChassis(Chassis chassis) {
        this.chassis = chassis;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public List<Wheel> getWheels() {
        return wheels;
    }

    public void setWheels(List<Wheel> wheels) {
        this.wheels = wheels;
    }

    @Override
    public String toString() {
        return "Car{" +
                "chassis=" + chassis +
                ", engine=" + engine +
                ", wheels=" + wheels +
                '}';
    }
}

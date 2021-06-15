package io.bhhoffmann.reactive.learning.model.vehicle;

public class Wheel {

    private String make;
    private Integer diameter;
    private Integer width;

    public Wheel(String make, Integer diameter, Integer width) {
        this.make = make;
        this.diameter = diameter;
        this.width = width;
    }

    public String getMake() {
        return make;
    }

    public Integer getDiameter() {
        return diameter;
    }

    public Integer getWidth() {
        return width;
    }

    @Override
    public String toString() {
        return "Wheel{" +
                "make='" + make + '\'' +
                ", diameter=" + diameter +
                ", width=" + width +
                '}';
    }
}

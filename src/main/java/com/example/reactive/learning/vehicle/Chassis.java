package com.example.reactive.learning.vehicle;

public class Chassis {

    private Integer chassisNumber;
    private String type;

    public Chassis(Integer chassisNumber, String type) {
        this.chassisNumber = chassisNumber;
        this.type = type;
    }

    public Integer getChassisNumber() {
        return chassisNumber;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Chassis{" +
                "chassisNumber=" + chassisNumber +
                ", type='" + type + '\'' +
                '}';
    }

}

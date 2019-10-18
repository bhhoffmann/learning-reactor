package com.example.reactive.learning.vehicle;

import org.springframework.stereotype.Component;

@Component
public class Engine {

    private Integer volume;
    private Integer hp;

    public Engine(Integer volume, Integer hp) {
        this.volume = volume;
        this.hp = hp;
    }

    public Integer getVolume() {
        return volume;
    }

    public Integer getHp() {
        return hp;
    }

    @Override
    public String toString() {
        return "Engine{" +
                "volume=" + volume +
                ", hp=" + hp +
                '}';
    }
}

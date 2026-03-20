package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;

@Entity
public class GestationalAge extends Age{
    private int age;

    public GestationalAge(int age) {
        this.age = age;
    }

    public GestationalAge() {
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}

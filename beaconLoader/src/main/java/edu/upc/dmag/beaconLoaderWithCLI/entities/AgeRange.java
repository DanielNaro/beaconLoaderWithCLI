package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;

import java.time.Duration;

@Entity
public class AgeRange extends Age{
    private Duration startAge;
    private Duration endAge;

    public AgeRange(Duration startAge, Duration endAge) {
        this.startAge = startAge;
        this.endAge = endAge;
    }

    public AgeRange() {
    }

    public Duration getStartAge() {
        return startAge;
    }

    public void setStartAge(Duration startAge) {
        this.startAge = startAge;
    }

    public Duration getEndAge() {
        return endAge;
    }

    public void setEndAge(Duration endAge) {
        this.endAge = endAge;
    }
}

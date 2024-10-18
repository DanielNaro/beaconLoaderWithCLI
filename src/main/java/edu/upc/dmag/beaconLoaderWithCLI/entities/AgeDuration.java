package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;

import java.time.Duration;

@Entity
public class AgeDuration extends Age{
    private Duration duration;

    public AgeDuration(Duration duration) {
        this.duration = duration;
    }

    public AgeDuration() {

    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }
}

package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.Duration;

@Entity
public class AgeRangeCriteria {

    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "start_age")
    Duration start;
    @Column(name = "end_age")
    Duration end;

    public void setId(Long id) {
        this.id = id;
    }

    public Duration getStart() {
        return start;
    }

    public void setStart(Duration start) {
        this.start = start;
    }

    public Duration getEnd() {
        return end;
    }

    public void setEnd(Duration end) {
        this.end = end;
    }

    public Long getId() {
        return id;
    }
}

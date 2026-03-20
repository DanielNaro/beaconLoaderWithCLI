package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Interval {
    @Id
    @GeneratedValue
    private Long id;

    private String type;
    private String start_pos;
    private String end_pos;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStart() {
        return start_pos;
    }

    public void setStart(String start) {
        this.start_pos = start;
    }

    public String getEnd() {
        return end_pos;
    }

    public void setEnd(String end) {
        this.end_pos = end;
    }
}

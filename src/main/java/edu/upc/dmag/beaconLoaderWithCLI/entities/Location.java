package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.*;

@Entity
public class Location {
    @Id
    @GeneratedValue
    private Long id;

    private String species_id;
    private String chr;
    @OneToOne(cascade = CascadeType.PERSIST)
    private Interval interval;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getSpecies_id() {
        return species_id;
    }

    public void setSpecies_id(String species_id) {
        this.species_id = species_id;
    }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }
}

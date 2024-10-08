package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class LibraryLayout {

    @Id
    private Long id;
    private String name;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

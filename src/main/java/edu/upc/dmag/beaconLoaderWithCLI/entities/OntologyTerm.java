package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class OntologyTerm {
    @Id
    String id;
    String label;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}

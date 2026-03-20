package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
public class DataUseConditions {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(cascade = CascadeType.PERSIST)
    Set<OntologyTerm> duoDataUses;

    public void setId(Long id) {
        this.id = id;
    }

    public Set<OntologyTerm> getDuoDataUses() {
        return duoDataUses;
    }

    public void setDuoDataUses(Set<OntologyTerm> duoDataUses) {
        this.duoDataUses = duoDataUses;
    }

    public Long getId() {
        return id;
    }
}

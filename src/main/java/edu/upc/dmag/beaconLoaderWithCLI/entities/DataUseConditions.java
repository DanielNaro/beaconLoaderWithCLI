package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
public class DataUseConditions {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany
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

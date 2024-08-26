package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class PhenotypicFeature {
    @ManyToOne
    OntologyTerm featureType;
    @ManyToMany
    Set<OntologyTerm> modifiers;
    @Id
    @GeneratedValue
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public OntologyTerm getFeatureType() {
        return featureType;
    }

    public void setFeatureType(OntologyTerm featureType) {
        this.featureType = featureType;
    }

    public Set<OntologyTerm> getModifiers() {
        return modifiers;
    }

    public void setModifiers(Set<OntologyTerm> modifiers) {
        this.modifiers = modifiers;
    }
}

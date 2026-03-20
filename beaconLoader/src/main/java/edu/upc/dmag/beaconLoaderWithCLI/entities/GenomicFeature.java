package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class GenomicFeature {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private OntologyTerm featureClass;
    @ManyToOne
    private OntologyTerm featureId;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public OntologyTerm getFeatureClass() {
        return featureClass;
    }

    public void setFeatureClass(OntologyTerm featureClass) {
        this.featureClass = featureClass;
    }

    public OntologyTerm getFeatureId() {
        return featureId;
    }

    public void setFeatureId(OntologyTerm featureId) {
        this.featureId = featureId;
    }
}

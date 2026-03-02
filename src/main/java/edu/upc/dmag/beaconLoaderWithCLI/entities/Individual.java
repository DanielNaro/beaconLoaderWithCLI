package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class Individual {
    @Id
    String id;
    @OneToMany
    Set<Measure> measures;
    @OneToMany
    Set<PhenotypicFeature> phenotypicFeatures;
    @ManyToOne
    OntologyTerm sex;
    @ElementCollection
    @Column(columnDefinition="TEXT")
    Set<String> allowedRoles;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<Measure> getMeasures() {
        return measures;
    }

    public void setMeasures(Set<Measure> measures) {
        this.measures = measures;
    }

    public Set<PhenotypicFeature> getPhenotypicFeatures() {
        return phenotypicFeatures;
    }

    public void setPhenotypicFeatures(Set<PhenotypicFeature> phenotypicFeatures) {
        this.phenotypicFeatures = phenotypicFeatures;
    }

    public OntologyTerm getSex() {
        return sex;
    }

    public void setSex(OntologyTerm sex) {
        this.sex = sex;
    }
}
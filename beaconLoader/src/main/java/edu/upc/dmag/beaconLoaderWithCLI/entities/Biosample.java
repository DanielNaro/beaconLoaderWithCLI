package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class Biosample {
    @Id
    String id;
    @ManyToOne
    Individual individual;
    @ManyToOne
    OntologyTerm biosampleStatus;
    @ManyToOne
    ObtentionProcedure obtentionProcedure;
    @ManyToOne
    OntologyTerm sampleOriginType;
    @ElementCollection
    @Column(columnDefinition="TEXT")
    Set<String> allowedRoles;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }

    public OntologyTerm getBiosampleStatus() {
        return biosampleStatus;
    }

    public void setBiosampleStatus(OntologyTerm biosampleStatus) {
        this.biosampleStatus = biosampleStatus;
    }

    public ObtentionProcedure getObtentionProcedure() {
        return obtentionProcedure;
    }

    public void setObtentionProcedure(ObtentionProcedure obtentionProcedure) {
        this.obtentionProcedure = obtentionProcedure;
    }

    public OntologyTerm getSampleOriginType() {
        return sampleOriginType;
    }

    public void setSampleOriginType(OntologyTerm sampleOriginType) {
        this.sampleOriginType = sampleOriginType;
    }
}

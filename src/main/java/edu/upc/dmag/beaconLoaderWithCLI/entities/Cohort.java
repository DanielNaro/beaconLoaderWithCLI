package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class Cohort {
    @Id
    String id;
    String name;
    @ManyToOne
    OntologyTerm cohortDesign;
    @ManyToOne
    CohortType cohortType;
    @ManyToMany
    Set<Individual> individuals;
    @OneToOne
    AgeRangeCriteria ageRangeInclusionCriteria;
    @ManyToMany
    Set<OntologyTerm> gendersInclusionCriteria;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OntologyTerm getCohortDesign() {
        return cohortDesign;
    }

    public void setCohortDesign(OntologyTerm cohortDesign) {
        this.cohortDesign = cohortDesign;
    }

    public CohortType getCohortType() {
        return cohortType;
    }

    public void setCohortType(CohortType cohortType) {
        this.cohortType = cohortType;
    }

    public Set<Individual> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(Set<Individual> individuals) {
        this.individuals = individuals;
    }

    public AgeRangeCriteria getAgeRangeInclusionCriteria() {
        return ageRangeInclusionCriteria;
    }

    public void setAgeRangeInclusionCriteria(AgeRangeCriteria ageRangeInclusionCriteria) {
        this.ageRangeInclusionCriteria = ageRangeInclusionCriteria;
    }

    public Set<OntologyTerm> getGendersInclusionCriteria() {
        return gendersInclusionCriteria;
    }

    public void setGendersInclusionCriteria(Set<OntologyTerm> gendersInclusionCriteria) {
        this.gendersInclusionCriteria = gendersInclusionCriteria;
    }
}
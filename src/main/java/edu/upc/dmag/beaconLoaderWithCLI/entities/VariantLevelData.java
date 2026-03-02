package edu.upc.dmag.beaconLoaderWithCLI.entities;


import jakarta.persistence.*;

import java.util.List;

@Entity
public class VariantLevelData {
    @OneToMany(cascade = CascadeType.PERSIST)
    private List<ClinicalInterpretation> clinicalInterpretations;

    @OneToMany(cascade = CascadeType.PERSIST)
    private List<ClinicalInterpretation> phenotypicEffects;
    @Id
    @GeneratedValue
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public List<ClinicalInterpretation> getClinicalInterpretations() {
        return clinicalInterpretations;
    }

    public void setClinicalInterpretations(List<ClinicalInterpretation> clinicalInterpretations) {
        this.clinicalInterpretations = clinicalInterpretations;
    }

    public List<ClinicalInterpretation> getPhenotypicEffects() {
        return phenotypicEffects;
    }

    public void setPhenotypicEffects(List<ClinicalInterpretation> phenotypicEffects) {
        this.phenotypicEffects = phenotypicEffects;
    }
}

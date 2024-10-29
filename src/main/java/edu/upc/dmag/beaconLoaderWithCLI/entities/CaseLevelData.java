package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

@Entity
public class CaseLevelData {
    @Id
    @GeneratedValue
    private int id;

    @ManyToOne
    private OntologyTerm alleleOrigin;
    @OneToMany
    private List<PhenotypicEffect> clinicalInterpretations;
    @OneToMany
    private List<PhenotypicEffect> phenotypicEffects;
    @ManyToOne
    private Analysis analysis;
    @ManyToOne
    private OntologyTerm zygosity;
    @ElementCollection
    @Column(columnDefinition="TEXT")
    Set<String> allowedRoles;
    private int depth;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public OntologyTerm getAlleleOrigin() {
        return alleleOrigin;
    }

    public void setAlleleOrigin(OntologyTerm alleleOrigin) {
        this.alleleOrigin = alleleOrigin;
    }

    public List<PhenotypicEffect> getClinicalInterpretations() {
        return clinicalInterpretations;
    }

    public void setClinicalInterpretations(List<PhenotypicEffect> clinicalInterpretations) {
        this.clinicalInterpretations = clinicalInterpretations;
    }

    public List<PhenotypicEffect> getPhenotypicEffects() {
        return phenotypicEffects;
    }

    public void setPhenotypicEffects(List<PhenotypicEffect> phenotypicEffects) {
        this.phenotypicEffects = phenotypicEffects;
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    public OntologyTerm getZygosity() {
        return zygosity;
    }

    public void setZygosity(OntologyTerm zygosity) {
        this.zygosity = zygosity;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}

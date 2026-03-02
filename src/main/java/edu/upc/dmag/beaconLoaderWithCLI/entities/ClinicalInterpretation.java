package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.*;

@Entity
public class ClinicalInterpretation {
    private String annotatedWithToolName;
    private String annotatedWithToolReference;
    private String annotatedWithToolVersion;
    @ManyToOne
    private OntologyTerm category;
    private edu.upc.dmag.ToLoad.ClinicalInterpretation.ClinicalRelevance clinicalRelevance;
    private String conditionId;
    @ManyToOne
    private OntologyTerm effect;
    @ManyToOne
    private OntologyTerm evidenceType;
    @Id
    @GeneratedValue
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }


    public String getAnnotatedWithToolName() {
        return annotatedWithToolName;
    }

    public void setAnnotatedWithToolName(String annotatedWithToolName) {
        this.annotatedWithToolName = annotatedWithToolName;
    }

    public String getAnnotatedWithToolReference() {
        return annotatedWithToolReference;
    }

    public void setAnnotatedWithToolReference(String annotatedWithToolReference) {
        this.annotatedWithToolReference = annotatedWithToolReference;
    }

    public String getAnnotatedWithToolVersion() {
        return annotatedWithToolVersion;
    }

    public void setAnnotatedWithToolVersion(String annotatedWithToolVersion) {
        this.annotatedWithToolVersion = annotatedWithToolVersion;
    }

    public OntologyTerm getCategory() {
        return category;
    }

    public void setCategory(OntologyTerm category) {
        this.category = category;
    }

    public edu.upc.dmag.ToLoad.ClinicalInterpretation.ClinicalRelevance getClinicalRelevance() {
        return clinicalRelevance;
    }

    public void setClinicalRelevance(edu.upc.dmag.ToLoad.ClinicalInterpretation.ClinicalRelevance clinicalRelevance) {
        this.clinicalRelevance = clinicalRelevance;
    }

    public String getConditionId() {
        return conditionId;
    }

    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }

    public OntologyTerm getEffect() {
        return effect;
    }

    public void setEffect(OntologyTerm effect) {
        this.effect = effect;
    }

    public OntologyTerm getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(OntologyTerm evidenceType) {
        this.evidenceType = evidenceType;
    }
}

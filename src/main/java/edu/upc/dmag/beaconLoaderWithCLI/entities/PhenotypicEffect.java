package edu.upc.dmag.beaconLoaderWithCLI.entities;

import edu.upc.dmag.ToLoad.ClinicalInterpretation;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class PhenotypicEffect {
    @Id
    @GeneratedValue
    private Long id;

    private String annotationToolName;
    private String annotationToolReference;
    private String annotationToolVersion;
    private ClinicalInterpretation.ClinicalRelevance clinicalRelevance;
    @ManyToOne
    private OntologyTerm category;
    private String conditionId;
    @ManyToOne
    private OntologyTerm effect;
    @ManyToOne
    private OntologyTerm evidenceType;


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getAnnotationToolName() {
        return annotationToolName;
    }

    public void setAnnotationToolName(String annotationToolName) {
        this.annotationToolName = annotationToolName;
    }

    public String getAnnotationToolReference() {
        return annotationToolReference;
    }

    public void setAnnotationToolReference(String annotationToolReference) {
        this.annotationToolReference = annotationToolReference;
    }

    public String getAnnotationToolVersion() {
        return annotationToolVersion;
    }

    public void setAnnotationToolVersion(String annotationToolVersion) {
        this.annotationToolVersion = annotationToolVersion;
    }

    public ClinicalInterpretation.ClinicalRelevance getClinicalRelevance() {
        return clinicalRelevance;
    }

    public void setClinicalRelevance(ClinicalInterpretation.ClinicalRelevance clinicalRelevance) {
        this.clinicalRelevance = clinicalRelevance;
    }

    public OntologyTerm getCategory() {
        return category;
    }

    public void setCategory(OntologyTerm category) {
        this.category = category;
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

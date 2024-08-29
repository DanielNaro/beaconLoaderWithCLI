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
}

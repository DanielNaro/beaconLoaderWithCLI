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
}

package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class CaseLevelData {
    @Id
    @GeneratedValue
    private Long id;

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

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}

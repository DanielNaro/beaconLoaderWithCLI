package edu.upc.dmag.beaconLoaderWithCLI.entities;



import jakarta.persistence.*;

import java.util.List;

@Entity
public class GenomicVariation {
    @Id
    private String variantInternalId;

    @OneToMany
    private List<CaseLevelData> caseLevelData;
    @OneToMany
    private List<FrequencyInPopulations> frequencyInPopulationsList;
    private String clinvarVariantId ;
    @ElementCollection
    private List<String> genomicHGVSId ;
    @ElementCollection
    private List<String> proteinHGVSIds;
    private String transcriptHGVSIds ;
    @OneToMany
    private List<VariantAlternativeId> variantAlternativeIds;
    @ElementCollection
    private List<String> aminoacidChanges ;
    @ElementCollection
    private List<String> geneIds ;
    @OneToMany
    private List<GenomicFeature> genomicFeatures ;
    @ManyToMany
    private List<OntologyTerm> molecularEffects;
    @OneToOne
    private VariantLevelData variantLevelData;
    @OneToOne
    private Variation variation;
}

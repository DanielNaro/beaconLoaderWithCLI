package edu.upc.dmag.beaconLoaderWithCLI.entities;



import edu.upc.dmag.beaconLoaderWithCLI.MolecularAttribute;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class GenomicVariation {
    @Id
    @Column(length=512)
    private String variantInternalId;

    @OneToMany
    private List<CaseLevelData> caseLevelData;
    @OneToMany
    private List<FrequencyInPopulations> frequencyInPopulationsList;
    @Column(length=512)
    private String clinvarVariantId ;
    @Column(length=513)
    private String genomicHGVSId ;
    @ElementCollection
    @Column(length=514)
    private List<String> proteinHGVSIds;
    @ElementCollection
    @Column(length=515)
    private List<String> transcriptHGVSIds ;
    @ManyToMany
    @Column(length=516)
    private List<VariantAlternativeId> variantAlternativeIds;
    @OneToOne
    private VariantLevelData variantLevelData;
    @OneToOne
    private Variation variation;
    @OneToMany
    private List<MolecularAttribute> molecularAttributes;

    public String getVariantInternalId() {
        return variantInternalId;
    }

    public void setVariantInternalId(String variantInternalId) {
        this.variantInternalId = variantInternalId;
    }

    public List<CaseLevelData> getCaseLevelData() {
        return caseLevelData;
    }

    public void setCaseLevelData(List<CaseLevelData> caseLevelData) {
        this.caseLevelData = caseLevelData;
    }

    public List<FrequencyInPopulations> getFrequencyInPopulationsList() {
        return frequencyInPopulationsList;
    }

    public void setFrequencyInPopulationsList(List<FrequencyInPopulations> frequencyInPopulationsList) {
        this.frequencyInPopulationsList = frequencyInPopulationsList;
    }

    public String getClinvarVariantId() {
        return clinvarVariantId;
    }

    public void setClinvarVariantId(String clinvarVariantId) {
        this.clinvarVariantId = clinvarVariantId;
    }

    public String getGenomicHGVSId() {
        return genomicHGVSId;
    }

    public void setGenomicHGVSId(String genomicHGVSId) {
        this.genomicHGVSId = genomicHGVSId;
    }

    public List<String> getProteinHGVSIds() {
        return proteinHGVSIds;
    }

    public void setProteinHGVSIds(List<String> proteinHGVSIds) {
        this.proteinHGVSIds = proteinHGVSIds;
    }

    public List<String> getTranscriptHGVSIds() {
        return transcriptHGVSIds;
    }

    public void setTranscriptHGVSIds(List<String> transcriptHGVSIds) {
        this.transcriptHGVSIds = transcriptHGVSIds;
    }

    public List<VariantAlternativeId> getVariantAlternativeIds() {
        return variantAlternativeIds;
    }

    public void setVariantAlternativeIds(List<VariantAlternativeId> variantAlternativeIds) {
        this.variantAlternativeIds = variantAlternativeIds;
    }


    public VariantLevelData getVariantLevelData() {
        return variantLevelData;
    }

    public void setVariantLevelData(VariantLevelData variantLevelData) {
        this.variantLevelData = variantLevelData;
    }

    public Variation getVariation() {
        return variation;
    }

    public void setVariation(Variation variation) {
        this.variation = variation;
    }

    public void setMolecularAttributes(List<MolecularAttribute> molecularAttributes) {
        this.molecularAttributes = molecularAttributes;
    }

    public List<MolecularAttribute> getMolecularAttributes() {
        return molecularAttributes;
    }
}

package edu.upc.dmag.beaconLoaderWithCLI;

import edu.upc.dmag.beaconLoaderWithCLI.entities.GenomicFeature;
import edu.upc.dmag.beaconLoaderWithCLI.entities.AnnotationImpact;
import edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm;
import jakarta.persistence.*;

@Entity
public class MolecularAttribute {
    @Id
    @Column(length=512)
    private String molecularAttributeId;
    @Column(length=517)
    private String aminoacidChange;
    private String geneId;
    @OneToOne
    private GenomicFeature genomicFeature;
    @OneToOne
    private OntologyTerm molecularEffect;
    @Enumerated(EnumType.ORDINAL)
    private AnnotationImpact annotationImpact;

    public void setAminoacidChange(String aminoacidChange) {
        this.aminoacidChange = aminoacidChange;
    }

    public String getAminoacidChange() {
        return aminoacidChange;
    }

    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    public String getGeneId() {
        return geneId;
    }

    public void setGenomicFeature(GenomicFeature genomicFeature) {
        this.genomicFeature = genomicFeature;
    }

    public GenomicFeature getGenomicFeature() {
        return genomicFeature;
    }

    public void setMolecularEffect(OntologyTerm molecularEffect) {
        this.molecularEffect = molecularEffect;
    }

    public OntologyTerm getMolecularEffect() {
        return molecularEffect;
    }

    public void setAnnotationImpact(AnnotationImpact annotationImpact) {
        this.annotationImpact = annotationImpact;
    }

    public AnnotationImpact getAnnotationImpact() {
        return annotationImpact;
    }
}

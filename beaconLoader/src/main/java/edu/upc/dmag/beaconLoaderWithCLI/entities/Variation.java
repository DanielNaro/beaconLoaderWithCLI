package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Variation {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Location location;
    @Column(length=1024)
    private String alternateBases;
    @Column(length=1025)
    private String referenceBases;
    @Column(length=1026)
    private String variantType;

    @ManyToMany
    private List<VariationHaplotypeMember> members;
    @OneToOne
    private Copies copies;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getAlternateBases() {
        return alternateBases;
    }

    public void setAlternateBases(String alternateBases) {
        this.alternateBases = alternateBases;
    }

    public String getReferenceBases() {
        return referenceBases;
    }

    public void setReferenceBases(String referenceBases) {
        this.referenceBases = referenceBases;
    }

    public String getVariantType() {
        return variantType;
    }

    public void setVariantType(String variantType) {
        this.variantType = variantType;
    }

    public List<VariationHaplotypeMember> getMembers() {
        return members;
    }

    public void setMembers(List<VariationHaplotypeMember> members) {
        this.members = members;
    }

    public Copies getCopies() {
        return copies;
    }

    public void setCopies(Copies copies) {
        this.copies = copies;
    }
}

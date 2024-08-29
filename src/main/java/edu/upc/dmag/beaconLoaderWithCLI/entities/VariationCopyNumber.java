package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;

import java.util.List;

@Entity
public class VariationCopyNumber extends Variation{
    @ManyToMany
    private List<VariationHaplotypeMember> members;
    @OneToOne
    private Copies copies;
}

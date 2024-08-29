package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;

import java.util.List;

@Entity
public class VariationHaplotype extends Variation{
    @ManyToMany
    private List<VariationHaplotypeMember> members;
}

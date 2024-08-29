package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

import java.util.List;

@Entity
public class VariationAlternateBases extends Variation{
    @ManyToOne
    private Location location;
    private String alternateBases;
    private String referenceBases;
    private String variantType;
}

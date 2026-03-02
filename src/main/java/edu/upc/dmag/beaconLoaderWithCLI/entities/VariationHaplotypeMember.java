package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;

@Entity
public class VariationHaplotypeMember {
    @Id
    private String id;
    @OneToOne(cascade = CascadeType.PERSIST)
    private Location location;
}

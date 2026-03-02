package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class VariationHaplotypeMember {
    @Id
    private String id;
    @OneToOne
    private Location location;
}

package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class VariantAlternativeId {
    @Id
    private String id;
    private String notes;
    private String reference;
}

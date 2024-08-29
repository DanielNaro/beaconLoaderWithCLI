package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Quantity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    ReferenceRange referenceRange;
    @ManyToOne
    OntologyTerm unit;
    private double value;

    public ReferenceRange getReferenceRange() {
        return referenceRange;
    }

    public void setReferenceRange(ReferenceRange referenceRange) {
        this.referenceRange = referenceRange;
    }

    public OntologyTerm getUnit() {
        return unit;
    }

    public void setUnit(OntologyTerm unit) {
        this.unit = unit;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}

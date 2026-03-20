package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.*;

@Entity
public class Value {
    @Id
    @GeneratedValue
    long id;
    @ManyToOne
    private OntologyTerm termValue;
    @OneToOne
    Quantity quantity;

    public Quantity getQuantity() {
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public OntologyTerm getTermValue() {
        return termValue;
    }

    public void setTermValue(OntologyTerm ontologyTerm) {
        this.termValue = ontologyTerm;
    }
}

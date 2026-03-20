package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.*;

@Entity
public class ComplexValue {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private Quantity quantity;
    @ManyToOne
    private OntologyTerm quantityType;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    public OntologyTerm getQuantityType() {
        return quantityType;
    }

    public void setQuantityType(OntologyTerm quantityType) {
        this.quantityType = quantityType;
    }
}

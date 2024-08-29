package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class MeasurementValue {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private ComplexValue complexValue;
    @OneToOne
    private Value value;

    public ComplexValue getComplexValue() {
        return complexValue;
    }

    public void setComplexValue(ComplexValue complexValue) {
        this.complexValue = complexValue;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}

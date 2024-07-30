package edu.upc.dmag.beaconLoaderWithCLI.entities;


public class MeasurementValueQuantity extends MeasurementValue{
    OntologyTerm unit;
    Float value;

    public OntologyTerm getUnit() {
        return unit;
    }

    public void setUnit(OntologyTerm unit) {
        this.unit = unit;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }
}

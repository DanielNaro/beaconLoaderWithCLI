package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.util.Objects;

@Entity
public class ReferenceRange {

    @Id
    @GeneratedValue
    private Long id;

    private double high;
    private double low;

    @ManyToOne
    OntologyTerm unit;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public OntologyTerm getUnit() {
        return unit;
    }

    public void setUnit(OntologyTerm unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ReferenceRange that = (ReferenceRange) object;
        return Double.compare(getHigh(), that.getHigh()) == 0 && Double.compare(getLow(), that.getLow()) == 0 && Objects.equals(getUnit(), that.getUnit());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHigh(), getLow(), getUnit());
    }
}

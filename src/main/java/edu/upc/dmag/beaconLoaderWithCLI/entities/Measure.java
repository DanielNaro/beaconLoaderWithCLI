package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.*;

import java.sql.Date;

@Entity
public class Measure {
    @ManyToOne
    OntologyTerm assayCode;
    Date date;
    @OneToOne
    MeasurementValue measurementValue;
    @Id
    @GeneratedValue
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public OntologyTerm getAssayCode() {
        return assayCode;
    }

    public void setAssayCode(OntologyTerm assayCode) {
        this.assayCode = assayCode;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public MeasurementValue getMeasurementValue() {
        return measurementValue;
    }

    public void setMeasurementValue(MeasurementValue measurementValue) {
        this.measurementValue = measurementValue;
    }
}

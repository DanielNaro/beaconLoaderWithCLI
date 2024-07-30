package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.Duration;

@Entity
public class ObtentionProcedure {
    @Id
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    Duration age;

    @ManyToOne
    OntologyTerm procedureCode;

    public Duration getAge() {
        return age;
    }

    public void setAge(Duration age) {
        this.age = age;
    }

    public OntologyTerm getProcedureCode() {
        return procedureCode;
    }

    public void setProcedureCode(OntologyTerm procedureCode) {
        this.procedureCode = procedureCode;
    }
}

package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.*;

import java.time.Duration;

@Entity
public class ObtentionProcedure {
    @Id
    @GeneratedValue
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @OneToOne
    Age age;

    @ManyToOne
    OntologyTerm procedureCode;

    public Age getAge() {
        return age;
    }

    public void setAge(Age age) {
        this.age = age;
    }

    public OntologyTerm getProcedureCode() {
        return procedureCode;
    }

    public void setProcedureCode(OntologyTerm procedureCode) {
        this.procedureCode = procedureCode;
    }
}

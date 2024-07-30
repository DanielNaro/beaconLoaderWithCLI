package edu.upc.dmag.beaconLoaderWithCLI.entities;

import edu.upc.dmag.ToLoad.RunsSchema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.sql.Date;

@Entity
public class Run {
    @Id
    String id;
    @ManyToOne
    Biosample biosample;
    @ManyToOne
    LibraryLayout libraryLayout;
    @ManyToOne
    LibrarySelection librarySelection;
    @ManyToOne
    OntologyTerm librarySource;
    String libraryStrategy;
    String platform;
    @ManyToOne
    OntologyTerm platformModel;
    Date runDate;
}

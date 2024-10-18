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
    RunsSchema.LibraryLayout libraryLayout;
    @ManyToOne
    LibrarySelection librarySelection;
    @ManyToOne
    OntologyTerm librarySource;
    String libraryStrategy;
    String platform;
    @ManyToOne
    OntologyTerm platformModel;
    Date runDate;

    public String getId() {
        return id;
    }

    public Biosample getBiosample() {
        return biosample;
    }

    public RunsSchema.LibraryLayout getLibraryLayout() {
        return libraryLayout;
    }

    public LibrarySelection getLibrarySelection() {
        return librarySelection;
    }

    public OntologyTerm getLibrarySource() {
        return librarySource;
    }

    public String getLibraryStrategy() {
        return libraryStrategy;
    }

    public String getPlatform() {
        return platform;
    }

    public OntologyTerm getPlatformModel() {
        return platformModel;
    }

    public Date getRunDate() {
        return runDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBiosample(Biosample biosample) {
        this.biosample = biosample;
    }

    public void setLibraryLayout(RunsSchema.LibraryLayout libraryLayout) {
        this.libraryLayout = libraryLayout;
    }

    public void setLibrarySelection(LibrarySelection librarySelection) {
        this.librarySelection = librarySelection;
    }

    public void setLibrarySource(OntologyTerm librarySource) {
        this.librarySource = librarySource;
    }

    public void setLibraryStrategy(String libraryStrategy) {
        this.libraryStrategy = libraryStrategy;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setPlatformModel(OntologyTerm platformModel) {
        this.platformModel = platformModel;
    }

    public void setRunDate(Date runDate) {
        this.runDate = runDate;
    }
}

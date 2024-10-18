package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.sql.Date;

@Entity
public class Analysis {
    @Id
    private String id;
    String aligner;
    Date analysisDate;
    String pipelineName;
    String pipelineRef;
    String variantCaller;
    @ManyToOne
    Run run;


    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getAligner() {
        return aligner;
    }

    public void setAligner(String aligner) {
        this.aligner = aligner;
    }

    public Date getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(Date analysisDate) {
        this.analysisDate = analysisDate;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public String getPipelineRef() {
        return pipelineRef;
    }

    public void setPipelineRef(String pipelineRef) {
        this.pipelineRef = pipelineRef;
    }

    public String getVariantCaller() {
        return variantCaller;
    }

    public void setVariantCaller(String variantCaller) {
        this.variantCaller = variantCaller;
    }

    public Run getRun() {
        return run;
    }

    public void setRun(Run run) {
        this.run = run;
    }
}

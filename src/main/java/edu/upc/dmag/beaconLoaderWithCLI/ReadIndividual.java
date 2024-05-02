package edu.upc.dmag.beaconLoaderWithCLI;

import org.openapitools.client.model.Individual;

public class ReadIndividual {
    private String id;
    private ReadMeasure[] measures;
    private ReadPhenotypicFeatures[] phenotypicFeatures;
    private ReadSex sex;

    public ReadIndividual(String id, ReadMeasure[] measures, ReadPhenotypicFeatures[] phenotypicFeatures, ReadSex readSex) {
        this.id = id;
        this.measures = measures;
        this.phenotypicFeatures = phenotypicFeatures;
        this.sex = readSex;
    }

    public Individual getAPIRepresentation() {
        Individual result = new Individual();
        //result.setSex(sex.getLabel());
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ReadMeasure[] getMeasures() {
        return measures;
    }

    public void setMeasures(ReadMeasure[] measures) {
        this.measures = measures;
    }

    public ReadPhenotypicFeatures[] getPhenotypicFeatures() {
        return phenotypicFeatures;
    }

    public void setPhenotypicFeatures(ReadPhenotypicFeatures[] phenotypicFeatures) {
        this.phenotypicFeatures = phenotypicFeatures;
    }

    public ReadSex getSex() {
        return sex;
    }

    public void setSex(ReadSex readSex) {
        this.sex = readSex;
    }
}

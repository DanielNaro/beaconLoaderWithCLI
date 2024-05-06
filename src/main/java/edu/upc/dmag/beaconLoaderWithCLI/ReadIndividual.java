package edu.upc.dmag.beaconLoaderWithCLI;

import org.openapitools.client.ApiException;
import org.openapitools.client.api.MeasuresItemResourceApi;
import org.openapitools.client.model.Individual;
import org.openapitools.client.model.MeasuresItem;
import org.openapitools.client.model.OntologyTerm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    public Individual getAPIRepresentation(
        Map<String, OntologyTerm> createdSexes,
        Map<String, OntologyTerm> createdAssayCodes) throws ApiException {
        List<MeasuresItem> measures = new ArrayList<>();
        for(var readMeasure: this.getMeasures()){
            MeasuresItem newMeasure = new MeasuresItem();
            newMeasure.setAssayCode(createdAssayCodes.get(readMeasure.getAssayCode().getId()));
            newMeasure.setBiosamplemeasurements();readMeasure.getMeasurementValue().getAPIRepresentation();
        }

        Individual result = new Individual();
        result.setIdAsProvided(id);
        result.setKaryotypicSex("");
        result.setEthnicity(null);
        result.setGeographicOrigins(List.of());
        result.setPedigrees(null);
        result.setPhenotypicFeatures(null);
        result.setInterventionsOrProcedures(null);
        result.setMeasures(measures);
        result.setDiseases(null);
        result.setExposures(List.of());
        result.setTreatments(List.of());
        result.setSequencingBioinformaticsAnalysisindividual(null);
        result.setBiosampleindividual(null);
        result.setRunindividual(null);
        result.setSex(createdSexes.get(sex.getId()));

        return result;
    }

    public List<MeasuresItem> getMeasures(MeasuresItemResourceApi measuresItemResourceApi, Individual createdIndividual) throws ApiException {
        List<MeasuresItem> toAdd = Arrays.stream(this.measures).map(it -> {
            MeasuresItem measuresItem = new MeasuresItem();
            measuresItem.setDate(it.getDate());
            measuresItem.setIndividualmeasures(List.of(createdIndividual));
            return measuresItem;
        }).toList();

        List<MeasuresItem> result = new ArrayList<>();
        for (MeasuresItem measuresItem: toAdd){
            result.add(measuresItemResourceApi.createMeasuresItem(measuresItem));
        }

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

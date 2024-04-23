package edu.upc.dmag.beaconLoaderWithCLI;

import org.openapitools.client.ApiException;
import org.openapitools.client.api.BiosampleObtentionProcedureResourceApi;
import org.openapitools.client.api.BiosampleStatusResourceApi;
import org.openapitools.client.model.Biosample;
import org.openapitools.client.model.BiosampleObtentionProcedure;
import org.openapitools.client.model.BiosampleStatus;

import java.util.Map;
import java.util.UUID;

public class ReadBiosample {
    private ReadBiosampleStatus biosampleStatus;
    private String id;
    private String individualId;
    private ReadObtentionProcedure obtentionProcedure;
    private ReadSampleOriginType sampleOriginType;

    public ReadBiosample(ReadBiosampleStatus biosampleStatus, String id, String individualId, ReadObtentionProcedure obtentionProcedure, ReadSampleOriginType sampleOriginType) {
        this.biosampleStatus = biosampleStatus;
        this.id = id;
        this.individualId = individualId;
        this.obtentionProcedure = obtentionProcedure;
        this.sampleOriginType = sampleOriginType;
    }

    public ReadBiosampleStatus getBiosampleStatus() {
        return biosampleStatus;
    }

    public void setBiosampleStatus(ReadBiosampleStatus biosampleStatus) {
        this.biosampleStatus = biosampleStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIndividualId() {
        return individualId;
    }

    public void setIndividualId(String individualId) {
        this.individualId = individualId;
    }

    public ReadObtentionProcedure getObtentionProcedure() {
        return obtentionProcedure;
    }

    public void setObtentionProcedure(ReadObtentionProcedure obtentionProcedure) {
        this.obtentionProcedure = obtentionProcedure;
    }

    public ReadSampleOriginType getSampleOriginType() {
        return sampleOriginType;
    }

    public void setSampleOriginType(ReadSampleOriginType sampleOriginType) {
        this.sampleOriginType = sampleOriginType;
    }

    public Biosample getAPIRepresentation(
            Map<ReadBiosampleStatus, UUID> createdBioSampleStatuses,
            Map<ReadObtentionProcedure, Long> createdBiosampleObtenitionProcedures,
            BiosampleStatusResourceApi biosampleStatusResourceApi,
            BiosampleObtentionProcedureResourceApi biosampleObtentionProcedureResourceApi
    ) throws ApiException {
        Biosample biosample = new Biosample();


        if (!createdBioSampleStatuses.containsKey(biosampleStatus)) {
            BiosampleStatus createdBiosampleStatus = biosampleStatusResourceApi.createBiosampleStatus(biosampleStatus.getAPIRepresentation());
            createdBioSampleStatuses.put(biosampleStatus, createdBiosampleStatus.getId());
        }
        biosample.addBiosampleStatusItem(createdBioSampleStatuses.get(biosampleStatus));

        if (!createdBiosampleObtenitionProcedures.containsKey(obtentionProcedure)) {
            var createdBiosampleStatus = biosampleObtentionProcedureResourceApi.createBiosampleObtentionProcedure(obtentionProcedure.getAPIRepresentation());
            createdBiosampleObtenitionProcedures.put(obtentionProcedure, createdBiosampleStatus.getId());
        }
        biosample.setBiosampleObtentionProcedure();
        biosample.addBiosampleObtentionProcedureItem(createdBiosampleObtenitionProcedures.get(obtentionProcedure));

        return biosample;
    }
}

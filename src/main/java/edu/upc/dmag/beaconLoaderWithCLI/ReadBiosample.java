package edu.upc.dmag.beaconLoaderWithCLI;

import org.openapitools.client.ApiException;
import org.openapitools.client.api.BiosampleObtentionProcedureResourceApi;
import org.openapitools.client.api.BiosampleStatusResourceApi;
import org.openapitools.client.model.*;

import java.util.Map;
import java.util.UUID;

public class ReadBiosample {
    private ReadBiosampleStatus biosampleStatus;
    private String id;
    private String individualId;
    private ReadObtentionProcedure obtentionProcedure;
    private ReadBiosampleOriginType biosampleOriginType;

    public ReadBiosample(ReadBiosampleStatus biosampleStatus, String id, String individualId, ReadObtentionProcedure obtentionProcedure, ReadBiosampleOriginType sampleOriginType) {
        this.biosampleStatus = biosampleStatus;
        this.id = id;
        this.individualId = individualId;
        this.obtentionProcedure = obtentionProcedure;
        this.biosampleOriginType = sampleOriginType;
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

    public ReadBiosampleOriginType getBiosampleOriginType() {
        return biosampleOriginType;
    }

    public void setBiosampleOriginType(ReadBiosampleOriginType biosampleOriginType) {
        this.biosampleOriginType = biosampleOriginType;
    }

    public Biosample getAPIRepresentation(
            Map<ReadBiosampleStatus, UUID> createdBioSampleStatuses,
            Map<ReadObtentionProcedure, UUID> createdBiosampleObtenitionProcedures,
            Map<ReadBiosampleOriginType, BiosampleSampleOrigin> createdBiosampleOrigins,
            BiosampleStatusResourceApi biosampleStatusResourceApi,
            BiosampleObtentionProcedureResourceApi biosampleObtentionProcedureResourceApi
    ) throws ApiException {
        Biosample biosample = new Biosample();


        if (!createdBioSampleStatuses.containsKey(biosampleStatus)) {
            BiosampleStatus createdBiosampleStatus = biosampleStatusResourceApi.createBiosampleStatus(biosampleStatus.getAPIRepresentation());
            createdBioSampleStatuses.put(biosampleStatus, createdBiosampleStatus.getId());
        }
        biosample.addBiosampleStatusItem(createdBioSampleStatuses.get(biosampleStatus));

        if (obtentionProcedure != null) {
            if (!createdBiosampleObtenitionProcedures.containsKey(obtentionProcedure)) {
                var createdBiosampleObtentionProcedure = biosampleObtentionProcedureResourceApi.createBiosampleObtentionProcedure(obtentionProcedure.getAPIRepresentation());
                createdBiosampleObtenitionProcedures.put(obtentionProcedure, createdBiosampleObtentionProcedure.getId());
            }
            biosample.biosampleObtentionProcedure(createdBiosampleObtenitionProcedures.get(obtentionProcedure));
        }

        if (biosampleOriginType != null) {
            if (!createdBiosampleOrigins.containsKey(biosampleOriginType)){
                biosample.setSampleOrigin(createdBiosampleOrigins.get(biosampleOriginType));
            } else {
                biosample.setSampleOrigin(biosampleOriginType.getAPIRepresentation());
            }
        }
        return biosample;
    }
}

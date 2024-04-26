package edu.upc.dmag.beaconLoaderWithCLI;

import org.openapitools.client.model.BiosampleObtentionProcedure;

public class ReadObtentionProcedure {
    private final ReadAgeAtProcedure ageAtProcedure;
    private final ReadProcedureCode procedureCode;
    private final ReadProcedureDuration procedureDuration;

    public ReadObtentionProcedure(ReadAgeAtProcedure ageAtProcedure, ReadProcedureCode procedureCode, ReadProcedureDuration procedureDuration) {
        this.ageAtProcedure = ageAtProcedure;
        this.procedureCode = procedureCode;
        this.procedureDuration = procedureDuration;
    }

    public BiosampleObtentionProcedure getAPIRepresentation() {
        BiosampleObtentionProcedure biosampleObtentionProcedure = new BiosampleObtentionProcedure();
        biosampleObtentionProcedure.setAgeAtProcedure(ConvertDuration.getDuration(ageAtProcedure.getAge().getIso8601duration()).toString());

        if (procedureDuration != null) {
            biosampleObtentionProcedure.setProcedureDuration(procedureDuration.getDuration().getIso8601duration());
        } else {
            biosampleObtentionProcedure.setProcedureDuration(null);
        }
        return biosampleObtentionProcedure;
    }
}

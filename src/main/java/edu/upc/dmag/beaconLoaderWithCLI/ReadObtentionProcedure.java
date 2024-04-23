package edu.upc.dmag.beaconLoaderWithCLI;

import org.openapitools.client.model.BiosampleObtentionProcedure;

public class ReadObtentionProcedure {
    private ReadAgeAtProcedure ageAtProcedure;
    private ReadProcedureCode procedureCode;

    public BiosampleObtentionProcedure getAPIRepresentation() {
        BiosampleObtentionProcedure biosampleObtentionProcedure = new BiosampleObtentionProcedure();
        biosampleObtentionProcedure.setAgeAtProcedure(ageAtProcedure.getAge().getIso8601duration());
        return biosampleObtentionProcedure;
    }
}

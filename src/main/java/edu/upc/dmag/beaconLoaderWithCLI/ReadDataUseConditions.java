package edu.upc.dmag.beaconLoaderWithCLI;

import org.openapitools.client.model.DataUseConditions;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ReadDataUseConditions {
    private ReadDuoDataUse[] duoDataUse;

    public DataUseConditions toApiRepresentation() {
        DataUseConditions dataUseConditions = new DataUseConditions();
        dataUseConditions.putAdditionalProperty("duoDataUse",
                Arrays
                        .stream(duoDataUse)
                        .map(it -> it.toApiRepresentation())
                        .collect(Collectors.toSet())
        );
        return dataUseConditions;
    }

    //public DataUseConditions
}

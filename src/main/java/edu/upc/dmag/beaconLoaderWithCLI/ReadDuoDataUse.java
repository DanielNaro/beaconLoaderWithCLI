package edu.upc.dmag.beaconLoaderWithCLI;

import org.openapitools.client.model.DuoDataUse;

public class ReadDuoDataUse {
    private String id;
    private String label;
    private String version;

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getVersion() {
        return version;
    }

    public DuoDataUse toApiRepresentation() {
        DuoDataUse duoDataUse = new DuoDataUse();
        return duoDataUse;
    }
}

package edu.upc.dmag.beaconLoaderWithCLI;

import org.openapitools.client.model.BiosampleStatus;

public class ReadBiosampleStatus {
    private String id;
    private String label;

    public BiosampleStatus getAPIRepresentation() {
        BiosampleStatus result = new BiosampleStatus();
        result.setLabel(label);
        return result;
    }
}

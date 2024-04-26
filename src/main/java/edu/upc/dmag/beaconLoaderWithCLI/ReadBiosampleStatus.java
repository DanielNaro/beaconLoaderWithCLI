package edu.upc.dmag.beaconLoaderWithCLI;

import org.openapitools.client.model.BiosampleStatus;

import java.util.UUID;

public class ReadBiosampleStatus {
    private String id;
    private String label;

    public ReadBiosampleStatus(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public BiosampleStatus getAPIRepresentation() {
        BiosampleStatus result = new BiosampleStatus();
        result.setIdString(getId());
        result.setLabel(label);
        return result;
    }
}

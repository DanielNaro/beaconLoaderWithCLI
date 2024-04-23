package edu.upc.dmag.beaconLoaderWithCLI;

import org.openapitools.client.model.BiosampleSampleOrigin;

public class ReadBiosampleOriginType {
    private String id;
    private String label;

    public ReadBiosampleOriginType(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BiosampleSampleOrigin getAPIRepresentation() {
        BiosampleSampleOrigin result = new BiosampleSampleOrigin();
        result.setLabel(label);
        return result;
    }
}

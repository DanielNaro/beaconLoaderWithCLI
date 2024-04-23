package edu.upc.dmag.beaconLoaderWithCLI;

public class ReadSampleOriginType {
    private String id;
    private String label;

    public ReadSampleOriginType(String id, String label) {
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
}

package edu.upc.dmag.beaconLoaderWithCLI;

public class ReadAge {
    private String iso8601duration;

    public ReadAge(String iso8601duration) {
        this.iso8601duration = iso8601duration;
    }

    public String getIso8601duration() {
        return iso8601duration;
    }

    public void setIso8601duration(String iso8601duration) {
        this.iso8601duration = iso8601duration;
    }
}

package edu.upc.dmag.beaconLoaderWithCLI;

import static edu.upc.dmag.beaconLoaderWithCLI.ConvertDuration.getDuration;

public class ReadDuration {
    private String iso8601duration;


    public ReadDuration(String iso8601duration) {
        this.iso8601duration = getDuration(iso8601duration).toString();
    }

    public String getIso8601duration() {
        return iso8601duration;
    }

    public void setIso8601duration(String iso8601duration) {
        this.iso8601duration = iso8601duration;
    }
}

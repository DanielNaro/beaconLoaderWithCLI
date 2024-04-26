package edu.upc.dmag.beaconLoaderWithCLI;

public class ReadProcedureDuration {
    private ReadDuration duration;

    public ReadProcedureDuration(ReadDuration duration) {
        this.duration = duration;
    }

    public ReadDuration getDuration() {
        return duration;
    }

    public void setDuration(ReadDuration duration) {
        this.duration = duration;
    }
}

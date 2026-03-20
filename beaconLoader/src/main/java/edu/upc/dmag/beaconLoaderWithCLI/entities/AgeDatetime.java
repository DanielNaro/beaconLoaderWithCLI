package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;

@Entity
public class AgeDatetime extends Age{
    private String dateTime;

    public AgeDatetime(String dateTime) {
        this.dateTime = dateTime;
    }

    public AgeDatetime() {
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}

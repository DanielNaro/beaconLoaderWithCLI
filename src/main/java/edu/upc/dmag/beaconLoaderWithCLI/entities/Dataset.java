package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;

import java.time.ZonedDateTime;
import java.util.Set;

@Entity
public class Dataset {
    @Id String id;
    String name;
    String version;
    boolean additionalProperties;
    String description;
    String externalUrl;
    ZonedDateTime createDateTime;
    ZonedDateTime updateDateTime;
    @OneToOne
    DataUseConditions dataUseConditions;

    @ManyToMany
    Set<Biosample> biosamples;
    @ManyToMany
    Set<Individual> individuals;

    String info_beacon_contact;
    String info_beacon_mapping;
    String info_beacon_version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(boolean additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public ZonedDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(ZonedDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public ZonedDateTime getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(ZonedDateTime updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public DataUseConditions getDataUseConditions() {
        return dataUseConditions;
    }

    public void setDataUseConditions(DataUseConditions dataUseConditions) {
        this.dataUseConditions = dataUseConditions;
    }

    public Set<Biosample> getBiosamples() {
        return biosamples;
    }

    public void setBiosamples(Set<Biosample> biosamples) {
        this.biosamples = biosamples;
    }

    public Set<Individual> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(Set<Individual> individuals) {
        this.individuals = individuals;
    }

    public String getInfo_beacon_contact() {
        return info_beacon_contact;
    }

    public void setInfo_beacon_contact(String info_beacon_contact) {
        this.info_beacon_contact = info_beacon_contact;
    }

    public String getInfo_beacon_mapping() {
        return info_beacon_mapping;
    }

    public void setInfo_beacon_mapping(String info_beacon_mapping) {
        this.info_beacon_mapping = info_beacon_mapping;
    }

    public String getInfo_beacon_version() {
        return info_beacon_version;
    }

    public void setInfo_beacon_version(String info_beacon_version) {
        this.info_beacon_version = info_beacon_version;
    }
}
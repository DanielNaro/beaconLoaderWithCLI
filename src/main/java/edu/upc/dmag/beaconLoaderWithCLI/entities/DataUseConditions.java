package edu.upc.dmag.beaconLoaderWithCLI.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
public class DataUseConditions {

    @Id
    private Long id;

    @OneToMany
    Set<DuoDataUse> duoDataUses;

    public void setId(Long id) {
        this.id = id;
    }

    public Set<DuoDataUse> getDuoDataUses() {
        return duoDataUses;
    }

    public void setDuoDataUses(Set<DuoDataUse> duoDataUses) {
        this.duoDataUses = duoDataUses;
    }

    public Long getId() {
        return id;
    }
}

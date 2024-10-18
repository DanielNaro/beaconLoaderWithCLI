package edu.upc.dmag.beaconLoaderWithCLI.entities;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BiosampleRepository extends JpaRepository<Biosample, String> {
}
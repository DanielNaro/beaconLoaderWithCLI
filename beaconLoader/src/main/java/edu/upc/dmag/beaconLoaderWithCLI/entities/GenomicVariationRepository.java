package edu.upc.dmag.beaconLoaderWithCLI.entities;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GenomicVariationRepository extends JpaRepository<GenomicVariation, String> {
}

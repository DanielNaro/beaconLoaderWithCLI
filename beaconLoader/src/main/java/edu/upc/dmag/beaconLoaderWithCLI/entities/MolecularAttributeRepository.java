package edu.upc.dmag.beaconLoaderWithCLI.entities;

import edu.upc.dmag.beaconLoaderWithCLI.MolecularAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MolecularAttributeRepository extends JpaRepository<MolecularAttribute, String> {
}

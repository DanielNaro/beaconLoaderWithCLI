package edu.upc.dmag.beaconLoaderWithCLI.entities;

import edu.upc.dmag.beaconLoaderWithCLI.MolecularAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariationHaplotypeMemberRepository extends JpaRepository<VariationHaplotypeMember, String> {
}

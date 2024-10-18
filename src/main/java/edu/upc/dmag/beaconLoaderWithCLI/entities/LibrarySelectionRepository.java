package edu.upc.dmag.beaconLoaderWithCLI.entities;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LibrarySelectionRepository extends JpaRepository<LibrarySelection, String> {
    Optional<LibrarySelection> findByName(String name);
}

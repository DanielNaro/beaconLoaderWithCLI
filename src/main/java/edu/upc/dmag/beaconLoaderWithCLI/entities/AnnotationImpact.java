package edu.upc.dmag.beaconLoaderWithCLI.entities;

public enum AnnotationImpact implements Comparable<AnnotationImpact> {
    LOW,
    MODERATE,
    HIGH;

    public static AnnotationImpact fromString(String value) {
        for (AnnotationImpact priority : AnnotationImpact.values()) {
            if (priority.name().equalsIgnoreCase(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown priority: " + value);
    }
}

package edu.upc.dmag.beaconLoaderWithCLI;

import org.openapitools.client.model.OntologyTerm;

public class ReadSex {
    private String id;
    private String label;

    public ReadSex(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public OntologyTerm toAPIRepresentation() {
        OntologyTerm ontologyTerm = new OntologyTerm();
        ontologyTerm.setLabel(label);
        ontologyTerm.setIdAsProvided(id);
        return ontologyTerm;
    }
}

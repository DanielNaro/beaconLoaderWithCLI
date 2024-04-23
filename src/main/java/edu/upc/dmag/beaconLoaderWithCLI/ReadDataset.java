package edu.upc.dmag.beaconLoaderWithCLI;

import org.openapitools.client.model.Dataset;

import java.lang.reflect.Array;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class ReadDataset {
    private String createDateTime;
    private String description;
    private String externalUrl;
    private String id;
    private Map<String, String[]> ids;
    private ReadDuoDataUse[] duoDataUses;

    public Dataset getAPIRepresentation() {
        Dataset result = new Dataset();
        result.setCreateDateTime(OffsetDateTime.parse(createDateTime));
        result.setDescription(description);
        result.externalUrl(externalUrl);
        result.setName(id);
        return result;
    }
}
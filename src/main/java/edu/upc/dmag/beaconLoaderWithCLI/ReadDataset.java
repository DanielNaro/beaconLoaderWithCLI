package edu.upc.dmag.beaconLoaderWithCLI;

import org.openapitools.client.model.DataUseConditions;
import org.openapitools.client.model.Dataset;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReadDataset {
    private String createDateTime;
    private String description;
    private String externalUrl;
    private String id;
    private Map<String, String[]> ids;
    private ReadDuoDataUse[] duoDataUses;
    private ReadDatasetInfo info;

    public Dataset getAPIRepresentation() {
        Dataset result = new Dataset();
        result.setCreateDateTime(createDateTime);
        result.setDescription(description);
        result.externalUrl(externalUrl);
        result.setName(id);
        Set<DataUseConditions> dataUseConditions = new HashSet<>();

        if (duoDataUses != null) {
            for (var duoDataUse : duoDataUses) {
                var newDataUseConditions = new DataUseConditions();
                //newDataUseConditions.setId(duoDataUse.getId());
                dataUseConditions.add(newDataUseConditions);
            }
        }

        result.setDataUseConditions(dataUseConditions);
        result.setIdAsProvided(id);
        //result.setVersion(this.);

        return result;
    }
}
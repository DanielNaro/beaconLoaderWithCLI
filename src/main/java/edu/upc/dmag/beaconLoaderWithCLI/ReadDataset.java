package edu.upc.dmag.beaconLoaderWithCLI;

import org.openapitools.client.model.DataUseConditions;
import org.openapitools.client.model.Dataset;

import java.util.Map;

public class ReadDataset {
    private String createDateTime;
    private String description;
    private String externalUrl;
    private String id;
    private Map<String, String[]> ids;

    public ReadDataUseConditions getDataUseConditions() {
        return dataUseConditions;
    }

    private ReadDataUseConditions dataUseConditions;
    private ReadDatasetInfo info;

    public Dataset getAPIRepresentation(DataUseConditions createdDataUseConditions) {
        Dataset result = new Dataset();
        result.setCreateDateTime(createDateTime);
        result.setDescription(description);
        result.externalUrl(externalUrl);
        result.setName(id);
        result.setVersion("1");
        result.setUpdateDateTime(createDateTime);

        result.setDataUseConditions(createdDataUseConditions);
        /*dataUseConditions.set

        result.setDataUseConditions(dataUseConditions);

        if (duoDataUses != null) {
            for (var duoDataUse : duoDataUses) {
                var newDataUseConditions = new DataUseConditions();

                //newDataUseConditions.setId(duoDataUse.getId());
                dataUseConditions.add(newDataUseConditions);
            }
        }*/

        //result.setDataUseConditions(dataUseConditions);
        result.setIdAsProvided(id);
        //result.setVersion(this.);

        return result;
    }
}
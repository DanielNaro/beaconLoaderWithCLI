package edu.upc.dmag.beaconLoaderWithCLI;

import edu.upc.dmag.ToLoad.DatasetsSchema;
import org.openapitools.client.model.DataUseConditions;
import org.openapitools.client.model.Dataset;

import java.util.HashSet;
import java.util.Set;

public class ToApiRepresentation {
    public static Dataset convertDataset(DatasetsSchema readDataset) {
        var result = new Dataset();

        result.setName(readDataset.getName());
        result.setDescription(readDataset.getDescription());
        result.setCreateDateTime(readDataset.getCreateDateTime().toString());
        result.setIdAsProvided(readDataset.getId());
        result.setVersion(readDataset.getVersion());
        result.setExternalUrl(readDataset.getExternalUrl());
        result.setUpdateDateTime(readDataset.getUpdateDateTime().toString());


        Set<DataUseConditions> dataUseConditions = new HashSet<>();
        var newDataUseCondition = new DataUseConditions();
        for (Object object:readDataset.getDataUseConditions().getDuoDataUse()){
            System.out.println(object);
        };

        result.setDataUseConditions(dataUseConditions);

        return result;
    }
}

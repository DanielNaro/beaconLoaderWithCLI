package edu.upc.dmag.beaconLoaderWithCLI.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DataLoadPathConfig {

    private final String basePath;

    public DataLoadPathConfig(@Value("${beacon.data.path:./src/main/resources/toLoad}") String basePath) {
        this.basePath = basePath;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getDatasetsPath() {
        return basePath + "/datasets.json";
    }

    public String getIndividualsPath() {
        return basePath + "/individuals.json";
    }

    public String getBiosamplesPath() {
        return basePath + "/biosamples.json";
    }

    public String getRunsPath() {
        return basePath + "/runs.json";
    }

    public String getAnalysesPath() {
        return basePath + "/analyses.json";
    }

    public String getCohortsPath() {
        return basePath + "/cohorts.json";
    }

    public String getGenomicVariationsPath() {
        return basePath + "/genomicVariationsVcf.json";
    }
}


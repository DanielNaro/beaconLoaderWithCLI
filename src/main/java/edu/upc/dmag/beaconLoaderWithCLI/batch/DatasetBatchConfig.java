package edu.upc.dmag.beaconLoaderWithCLI.batch;

import com.google.gson.Gson;
import edu.upc.dmag.ToLoad.DatasetsSchema;
import edu.upc.dmag.ToLoad.DuoDataUse;
import edu.upc.dmag.beaconLoaderWithCLI.entities.DataUseConditions;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Dataset;
import edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm;
import edu.upc.dmag.beaconLoaderWithCLI.entities.DataUseConditionsRepository;
import edu.upc.dmag.beaconLoaderWithCLI.entities.DatasetRepository;
import edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTermRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

@Configuration
public class DatasetBatchConfig {

    private final DatasetRepository datasetRepository;
    private final DataUseConditionsRepository dataUseConditionsRepository;
    private final OntologyTermRepository ontologyTermRepository;

    public DatasetBatchConfig(DatasetRepository datasetRepository,
                              DataUseConditionsRepository dataUseConditionsRepository,
                              OntologyTermRepository ontologyTermRepository) {
        this.datasetRepository = datasetRepository;
        this.dataUseConditionsRepository = dataUseConditionsRepository;
        this.ontologyTermRepository = ontologyTermRepository;
    }

    @Bean
    public Job loadDatasetsJob(JobRepository jobRepository, Step loadDatasetsStep) {
        return new JobBuilder("loadDatasetsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(loadDatasetsStep)
                .build();
    }

    @Bean
    public Step loadDatasetsStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  ItemReader<DatasetsSchema> datasetReader,
                                  ItemProcessor<DatasetsSchema, Dataset> datasetProcessor,
                                  ItemWriter<Dataset> datasetWriter) {
        return new StepBuilder("loadDatasetsStep", jobRepository)
                .<DatasetsSchema, Dataset>chunk(10, transactionManager)
                .reader(datasetReader)
                .processor(datasetProcessor)
                .writer(datasetWriter)
                .build();
    }

    @Bean
    public ItemReader<DatasetsSchema> datasetReader() {
        return new DatasetJsonItemReader("./src/main/resources/toLoad/datasets.json");
    }

    @Bean
    public ItemProcessor<DatasetsSchema, Dataset> datasetProcessor() {
        return readDataset -> {
            var dataset = new Dataset();
            dataset.setId(readDataset.getId());
            dataset.setName(readDataset.getName());
            dataset.setDescription(readDataset.getDescription());
            dataset.setVersion(readDataset.getVersion());
            dataset.setExternalUrl(readDataset.getExternalUrl());
            dataset.setCreateDateTime(ZonedDateTime.parse(readDataset.getCreateDateTime()));
            dataset.setUpdateDateTime(ZonedDateTime.parse(readDataset.getUpdateDateTime()));
            dataset.setDataUseConditions(getDatauseConditions(readDataset.getDataUseConditions()));
            dataset.setInfo_beacon_version(readDataset.getInfo().getBeacon().getVersion());
            dataset.setInfo_beacon_mapping(readDataset.getInfo().getBeacon().getMapping());
            dataset.setInfo_beacon_contact(readDataset.getInfo().getBeacon().getContact());
            return dataset;
        };
    }

    @Bean
    public ItemWriter<Dataset> datasetWriter() {
        return datasets -> {
            for (Dataset dataset : datasets) {
                datasetRepository.save(dataset);
            }
        };
    }

    private DataUseConditions getDatauseConditions(edu.upc.dmag.ToLoad.DataUseConditions readDataUseConditions) {
        var dataUseConditions = new DataUseConditions();
        dataUseConditions.setDuoDataUses(
                readDataUseConditions.getDuoDataUse().stream()
                        .map(this::getOntologyTerm)
                        .collect(Collectors.toSet())
        );
        dataUseConditionsRepository.save(dataUseConditions);
        return dataUseConditions;
    }

    private OntologyTerm getOntologyTerm(DuoDataUse it) {
        var foundTerm = ontologyTermRepository.findById(it.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(it.getId());
            ontologyTerm.setLabel(it.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    /**
     * Custom ItemReader that reads DatasetsSchema objects from a JSON array file.
     */
    public static class DatasetJsonItemReader implements ItemReader<DatasetsSchema> {
        private final String filePath;
        private Iterator<DatasetsSchema> datasetIterator;
        private boolean initialized = false;

        public DatasetJsonItemReader(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public DatasetsSchema read() {
            if (!initialized) {
                initialize();
            }
            if (datasetIterator != null && datasetIterator.hasNext()) {
                return datasetIterator.next();
            }
            return null;
        }

        private void initialize() {
            try {
                InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream(filePath));
                Gson gson = new Gson();
                DatasetsSchema[] readDatasets = gson.fromJson(jsonFileInputStream, DatasetsSchema[].class);
                datasetIterator = Arrays.asList(readDatasets).iterator();
                initialized = true;
            } catch (IOException e) {
                throw new RuntimeException("Error reading datasets JSON file: " + filePath, e);
            }
        }
    }
}


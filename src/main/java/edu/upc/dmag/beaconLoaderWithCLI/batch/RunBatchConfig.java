package edu.upc.dmag.beaconLoaderWithCLI.batch;

import com.google.gson.Gson;
import edu.upc.dmag.ToLoad.LibrarySource;
import edu.upc.dmag.ToLoad.PlatformModel;
import edu.upc.dmag.ToLoad.RunsSchema;
import edu.upc.dmag.beaconLoaderWithCLI.entities.BiosampleRepository;
import edu.upc.dmag.beaconLoaderWithCLI.entities.LibrarySelection;
import edu.upc.dmag.beaconLoaderWithCLI.entities.LibrarySelectionRepository;
import edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm;
import edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTermRepository;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Run;
import edu.upc.dmag.beaconLoaderWithCLI.entities.RunRepository;
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
import java.sql.Date;
import java.util.Arrays;
import java.util.Iterator;

@Configuration
public class RunBatchConfig {

    private final RunRepository runRepository;
    private final BiosampleRepository biosampleRepository;
    private final OntologyTermRepository ontologyTermRepository;
    private final LibrarySelectionRepository librarySelectionRepository;

    public RunBatchConfig(RunRepository runRepository,
                          BiosampleRepository biosampleRepository,
                          OntologyTermRepository ontologyTermRepository,
                          LibrarySelectionRepository librarySelectionRepository) {
        this.runRepository = runRepository;
        this.biosampleRepository = biosampleRepository;
        this.ontologyTermRepository = ontologyTermRepository;
        this.librarySelectionRepository = librarySelectionRepository;
    }

    @Bean
    public Job loadRunsJob(JobRepository jobRepository, Step loadRunsStep) {
        return new JobBuilder("loadRunsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(loadRunsStep)
                .build();
    }

    @Bean
    public Step loadRunsStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              ItemReader<RunsSchema> runReader,
                              ItemProcessor<RunsSchema, Run> runProcessor,
                              ItemWriter<Run> runWriter) {
        return new StepBuilder("loadRunsStep", jobRepository)
                .<RunsSchema, Run>chunk(10, transactionManager)
                .reader(runReader)
                .processor(runProcessor)
                .writer(runWriter)
                .build();
    }

    @Bean
    public ItemReader<RunsSchema> runReader() {
        return new RunJsonItemReader("./src/main/resources/toLoad/runs.json");
    }

    @Bean
    public ItemProcessor<RunsSchema, Run> runProcessor() {
        return readRun -> {
            var run = new Run();
            run.setId(readRun.getId());
            run.setRunDate(Date.valueOf(readRun.getRunDate()));
            run.setPlatformModel(getOntologyTerm(readRun.getPlatformModel()));
            run.setLibraryStrategy(readRun.getLibraryStrategy());
            run.setPlatform(readRun.getPlatform());
            run.setLibraryLayout(readRun.getLibraryLayout());
            run.setLibrarySelection(getLibrarySelection(readRun.getLibrarySelection()));
            run.setLibrarySource(getOntologyTerm(readRun.getLibrarySource()));
            run.setBiosample(biosampleRepository.getReferenceById(readRun.getBiosampleId()));
            return run;
        };
    }

    @Bean
    public ItemWriter<Run> runWriter() {
        return runs -> {
            for (Run run : runs) {
                runRepository.save(run);
            }
        };
    }

    private OntologyTerm getOntologyTerm(PlatformModel platformModel) {
        var foundTerm = ontologyTermRepository.findById(platformModel.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(platformModel.getId());
            ontologyTerm.setLabel(platformModel.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    private OntologyTerm getOntologyTerm(LibrarySource librarySource) {
        var foundTerm = ontologyTermRepository.findById(librarySource.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(librarySource.getId());
            ontologyTerm.setLabel(librarySource.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    private LibrarySelection getLibrarySelection(String librarySelectionName) {
        var foundLibrarySelection = librarySelectionRepository.findByName(librarySelectionName);
        if (foundLibrarySelection.isPresent()) {
            return foundLibrarySelection.get();
        } else {
            LibrarySelection newLibrarySelection = new LibrarySelection();
            newLibrarySelection.setName(librarySelectionName);
            librarySelectionRepository.save(newLibrarySelection);
            return newLibrarySelection;
        }
    }

    /**
     * Custom ItemReader that reads RunsSchema objects from a JSON array file.
     */
    public static class RunJsonItemReader implements ItemReader<RunsSchema> {
        private final String filePath;
        private Iterator<RunsSchema> runIterator;
        private boolean initialized = false;

        public RunJsonItemReader(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public RunsSchema read() {
            if (!initialized) {
                initialize();
            }
            if (runIterator != null && runIterator.hasNext()) {
                return runIterator.next();
            }
            return null;
        }

        private void initialize() {
            try {
                InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream(filePath));
                Gson gson = new Gson();
                RunsSchema[] readRuns = gson.fromJson(jsonFileInputStream, RunsSchema[].class);
                runIterator = Arrays.asList(readRuns).iterator();
                initialized = true;
            } catch (IOException e) {
                throw new RuntimeException("Error reading runs JSON file: " + filePath, e);
            }
        }
    }
}


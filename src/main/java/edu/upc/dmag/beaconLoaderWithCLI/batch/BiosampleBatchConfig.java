package edu.upc.dmag.beaconLoaderWithCLI.batch;

import com.google.gson.Gson;
import edu.upc.dmag.ToLoad.AgeRange;
import edu.upc.dmag.ToLoad.BiosampleStatus;
import edu.upc.dmag.ToLoad.BiosamplesSchema;
import edu.upc.dmag.ToLoad.ObtentionProcedure;
import edu.upc.dmag.ToLoad.ProcedureCode__1;
import edu.upc.dmag.ToLoad.SampleOriginType;
import edu.upc.dmag.beaconLoaderWithCLI.ConvertDuration;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Age;
import edu.upc.dmag.beaconLoaderWithCLI.entities.AgeDatetime;
import edu.upc.dmag.beaconLoaderWithCLI.entities.AgeDuration;
import edu.upc.dmag.beaconLoaderWithCLI.entities.AgeRepository;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Biosample;
import edu.upc.dmag.beaconLoaderWithCLI.entities.BiosampleRepository;
import edu.upc.dmag.beaconLoaderWithCLI.entities.GestationalAge;
import edu.upc.dmag.beaconLoaderWithCLI.entities.IndividualRepository;
import edu.upc.dmag.beaconLoaderWithCLI.entities.ObtentionProcedureRepository;
import edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm;
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
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;

@Configuration
public class BiosampleBatchConfig {

    private final BiosampleRepository biosampleRepository;
    private final OntologyTermRepository ontologyTermRepository;
    private final ObtentionProcedureRepository obtentionProcedureRepository;
    private final IndividualRepository individualRepository;
    private final AgeRepository ageRepository;

    public BiosampleBatchConfig(
            BiosampleRepository biosampleRepository,
            OntologyTermRepository ontologyTermRepository,
            ObtentionProcedureRepository obtentionProcedureRepository,
            IndividualRepository individualRepository,
            AgeRepository ageRepository) {
        this.biosampleRepository = biosampleRepository;
        this.ontologyTermRepository = ontologyTermRepository;
        this.obtentionProcedureRepository = obtentionProcedureRepository;
        this.individualRepository = individualRepository;
        this.ageRepository = ageRepository;
    }

    @Bean
    public Job loadBiosamplesJob(JobRepository jobRepository, Step loadBiosamplesStep) {
        return new JobBuilder("loadBiosamplesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(loadBiosamplesStep)
                .build();
    }

    @Bean
    public Step loadBiosamplesStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    ItemReader<BiosamplesSchema> biosampleReader,
                                    ItemProcessor<BiosamplesSchema, Biosample> biosampleProcessor,
                                    ItemWriter<Biosample> biosampleWriter) {
        return new StepBuilder("loadBiosamplesStep", jobRepository)
                .<BiosamplesSchema, Biosample>chunk(10, transactionManager)
                .reader(biosampleReader)
                .processor(biosampleProcessor)
                .writer(biosampleWriter)
                .build();
    }

    @Bean
    public ItemReader<BiosamplesSchema> biosampleReader() {
        return new BiosampleJsonItemReader("./src/main/resources/toLoad/biosamples.json");
    }

    @Bean
    public ItemProcessor<BiosamplesSchema, Biosample> biosampleProcessor() {
        return readBiosample -> {
            var biosample = new Biosample();
            biosample.setId(readBiosample.getId());
            biosample.setObtentionProcedure(getObtentionProcedure(readBiosample.getObtentionProcedure()));
            biosample.setBiosampleStatus(getOntologyTerm(readBiosample.getBiosampleStatus()));
            biosample.setSampleOriginType(getOntologyTerm(readBiosample.getSampleOriginType()));
            biosample.setIndividual(individualRepository.getReferenceById(readBiosample.getIndividualId()));
            return biosample;
        };
    }

    @Bean
    public ItemWriter<Biosample> biosampleWriter() {
        return biosamples -> {
            for (Biosample biosample : biosamples) {
                biosampleRepository.save(biosample);
            }
        };
    }

    // Helper methods for processing

    private OntologyTerm getOntologyTerm(SampleOriginType sampleOriginType) {
        var foundTerm = ontologyTermRepository.findById(sampleOriginType.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(sampleOriginType.getId());
            ontologyTerm.setLabel(sampleOriginType.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.ObtentionProcedure getObtentionProcedure(ObtentionProcedure readObtentionProcedure) {
        var obtentionProcedure = new edu.upc.dmag.beaconLoaderWithCLI.entities.ObtentionProcedure();
        obtentionProcedure.setProcedureCode(getOntologyTerm(readObtentionProcedure.getProcedureCode()));
        if (readObtentionProcedure.getAgeAtProcedure().getAge() != null) {
            obtentionProcedure.setAge(
                getAgeDuration(ConvertDuration.getDuration(readObtentionProcedure.getAgeAtProcedure().getAge().getIso8601duration()))
            );
        } else if (readObtentionProcedure.getAgeAtProcedure().getAgeRange() != null) {
            obtentionProcedure.setAge(getAgeRange(readObtentionProcedure.getAgeAtProcedure().getAgeRange()));
        } else if (readObtentionProcedure.getAgeAtProcedure().getGestationalAge() != null) {
            obtentionProcedure.setAge(
                    getGestationalAge(readObtentionProcedure.getAgeAtProcedure().getGestationalAge())
            );
        } else {
            obtentionProcedure.setAge(
                getAgeDatetime(readObtentionProcedure.getAgeAtProcedure().getDateTime().toString())
            );
        }
        obtentionProcedureRepository.save(obtentionProcedure);
        return obtentionProcedure;
    }

    private Age getAgeDatetime(String string) {
        var ageDatetime = new AgeDatetime(string);
        ageRepository.save(ageDatetime);
        return ageDatetime;
    }

    private Age getGestationalAge(Integer readGestationalAge) {
        var gestationalAge = new GestationalAge(readGestationalAge);
        ageRepository.save(gestationalAge);
        return gestationalAge;
    }

    private Age getAgeRange(AgeRange readAgeRange) {
        var ageRange = new edu.upc.dmag.beaconLoaderWithCLI.entities.AgeRange(
                ConvertDuration.getDuration(readAgeRange.getStart()),
                ConvertDuration.getDuration(readAgeRange.getEnd())
        );
        ageRepository.save(ageRange);
        return ageRange;
    }

    private Age getAgeDuration(Duration duration) {
        var ageDuration = new AgeDuration(duration);
        ageRepository.save(ageDuration);
        return ageDuration;
    }

    private OntologyTerm getOntologyTerm(ProcedureCode__1 procedureCode) {
        var foundTerm = ontologyTermRepository.findById(procedureCode.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(procedureCode.getId());
            ontologyTerm.setLabel(procedureCode.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    private OntologyTerm getOntologyTerm(BiosampleStatus biosampleStatus) {
        var foundTerm = ontologyTermRepository.findById(biosampleStatus.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(biosampleStatus.getId());
            ontologyTerm.setLabel(biosampleStatus.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    /**
     * Custom ItemReader that reads BiosamplesSchema objects from a JSON array file.
     */
    public static class BiosampleJsonItemReader implements ItemReader<BiosamplesSchema> {
        private final String filePath;
        private Iterator<BiosamplesSchema> biosampleIterator;
        private boolean initialized = false;

        public BiosampleJsonItemReader(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public BiosamplesSchema read() {
            if (!initialized) {
                initialize();
            }
            if (biosampleIterator != null && biosampleIterator.hasNext()) {
                return biosampleIterator.next();
            }
            return null;
        }

        private void initialize() {
            try {
                InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream(filePath));
                Gson gson = new Gson();
                BiosamplesSchema[] readBiosamples = gson.fromJson(jsonFileInputStream, BiosamplesSchema[].class);
                biosampleIterator = Arrays.asList(readBiosamples).iterator();
                initialized = true;
            } catch (IOException e) {
                throw new RuntimeException("Error reading biosamples JSON file: " + filePath, e);
            }
        }
    }
}


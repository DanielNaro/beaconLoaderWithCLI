package edu.upc.dmag.beaconLoaderWithCLI.batch;

import com.google.gson.Gson;
import edu.upc.dmag.ToLoad.AgeRange__2;
import edu.upc.dmag.ToLoad.CohortDesign;
import edu.upc.dmag.ToLoad.CohortsSchema;
import edu.upc.dmag.ToLoad.Gender__1;
import edu.upc.dmag.beaconLoaderWithCLI.ConvertDuration;
import edu.upc.dmag.beaconLoaderWithCLI.entities.AgeRangeCriteria;
import edu.upc.dmag.beaconLoaderWithCLI.entities.AgeRangeCriteriaRepository;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Cohort;
import edu.upc.dmag.beaconLoaderWithCLI.entities.CohortRepository;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class CohortBatchConfig {

    private final CohortRepository cohortRepository;
    private final AgeRangeCriteriaRepository ageRangeCriteriaRepository;
    private final OntologyTermRepository ontologyTermRepository;

    public CohortBatchConfig(CohortRepository cohortRepository,
                             AgeRangeCriteriaRepository ageRangeCriteriaRepository,
                             OntologyTermRepository ontologyTermRepository) {
        this.cohortRepository = cohortRepository;
        this.ageRangeCriteriaRepository = ageRangeCriteriaRepository;
        this.ontologyTermRepository = ontologyTermRepository;
    }

    @Bean
    public Job loadCohortsJob(JobRepository jobRepository, Step loadCohortsStep) {
        return new JobBuilder("loadCohortsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(loadCohortsStep)
                .build();
    }

    @Bean
    public Step loadCohortsStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 ItemReader<CohortsSchema> cohortReader,
                                 ItemProcessor<CohortsSchema, Cohort> cohortProcessor,
                                 ItemWriter<Cohort> cohortWriter) {
        return new StepBuilder("loadCohortsStep", jobRepository)
                .<CohortsSchema, Cohort>chunk(10, transactionManager)
                .reader(cohortReader)
                .processor(cohortProcessor)
                .writer(cohortWriter)
                .build();
    }

    @Bean
    public ItemReader<CohortsSchema> cohortReader() {
        return new CohortJsonItemReader("./src/main/resources/toLoad/cohorts.json");
    }

    @Bean
    public ItemProcessor<CohortsSchema, Cohort> cohortProcessor() {
        return readCohort -> {
            Cohort cohort = new Cohort();
            cohort.setId(readCohort.getId());
            cohort.setName(readCohort.getName());
            cohort.setCohortDesign(getOntologyTerm(readCohort.getCohortDesign()));
            cohort.setCohortType(readCohort.getCohortType());
            cohort.setGendersInclusionCriteria(getOntologyTermsGenderInclusion(readCohort.getInclusionCriteria().getGenders()));
            cohort.setAgeRangeInclusionCriteria(getAgeRange(readCohort.getInclusionCriteria().getAgeRange()));
            return cohort;
        };
    }

    @Bean
    public ItemWriter<Cohort> cohortWriter() {
        return cohorts -> {
            for (Cohort cohort : cohorts) {
                cohortRepository.save(cohort);
            }
        };
    }

    private AgeRangeCriteria getAgeRange(AgeRange__2 ageRange) {
        AgeRangeCriteria ageRangeCriteria = new AgeRangeCriteria();
        ageRangeCriteria.setStart(ConvertDuration.getDuration(ageRange.getStart().getIso8601duration()));
        ageRangeCriteria.setEnd(ConvertDuration.getDuration(ageRange.getEnd().getIso8601duration()));
        ageRangeCriteriaRepository.save(ageRangeCriteria);
        return ageRangeCriteria;
    }

    private Set<OntologyTerm> getOntologyTermsGenderInclusion(List<Gender__1> genders) {
        return genders.stream().map(this::getOntologyTerm).collect(Collectors.toSet());
    }

    private OntologyTerm getOntologyTerm(Gender__1 it) {
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

    private OntologyTerm getOntologyTerm(CohortDesign cohortDesign) {
        var foundTerm = ontologyTermRepository.findById(cohortDesign.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(cohortDesign.getId());
            ontologyTerm.setLabel(cohortDesign.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    /**
     * Custom ItemReader that reads CohortsSchema objects from a JSON array file.
     */
    public static class CohortJsonItemReader implements ItemReader<CohortsSchema> {
        private final String filePath;
        private Iterator<CohortsSchema> cohortIterator;
        private boolean initialized = false;

        public CohortJsonItemReader(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public CohortsSchema read() {
            if (!initialized) {
                initialize();
            }
            if (cohortIterator != null && cohortIterator.hasNext()) {
                return cohortIterator.next();
            }
            return null;
        }

        private void initialize() {
            try {
                InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream(filePath));
                Gson gson = new Gson();
                CohortsSchema[] readCohorts = gson.fromJson(jsonFileInputStream, CohortsSchema[].class);
                cohortIterator = Arrays.asList(readCohorts).iterator();
                initialized = true;
            } catch (IOException e) {
                throw new RuntimeException("Error reading cohorts JSON file: " + filePath, e);
            }
        }
    }
}


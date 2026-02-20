package edu.upc.dmag.beaconLoaderWithCLI.batch;

import com.google.gson.Gson;
import edu.upc.dmag.ToLoad.AssayCode__1;
import edu.upc.dmag.ToLoad.FeatureType__3;
import edu.upc.dmag.ToLoad.IndividualsSchema;
import edu.upc.dmag.ToLoad.Modifier__3;
import edu.upc.dmag.ToLoad.PhenotypicFeature__1;
import edu.upc.dmag.ToLoad.Quantity__1;
import edu.upc.dmag.ToLoad.QuantityType;
import edu.upc.dmag.ToLoad.Sex;
import edu.upc.dmag.ToLoad.Unit__1;
import edu.upc.dmag.ToLoad.Unit__2;
import edu.upc.dmag.ToLoad.Unit__3;
import edu.upc.dmag.ToLoad.Unit__4;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Individual;
import edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class IndividualBatchConfig {

    private final edu.upc.dmag.beaconLoaderWithCLI.entities.IndividualRepository individualRepository;
    private final edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTermRepository ontologyTermRepository;
    private final edu.upc.dmag.beaconLoaderWithCLI.entities.MeasureRepository measureRepository;
    private final edu.upc.dmag.beaconLoaderWithCLI.entities.MeasurementValueRepository measurementValueRepository;
    private final edu.upc.dmag.beaconLoaderWithCLI.entities.ValueRepository valueRepository;
    private final edu.upc.dmag.beaconLoaderWithCLI.entities.QuantityRepository quantityRepository;
    private final edu.upc.dmag.beaconLoaderWithCLI.entities.ComplexValueRepository complexValueRepository;
    private final edu.upc.dmag.beaconLoaderWithCLI.entities.PhenotypicFeatureRepository phenotypicFeatureRepository;

    public IndividualBatchConfig(
            edu.upc.dmag.beaconLoaderWithCLI.entities.IndividualRepository individualRepository,
            edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTermRepository ontologyTermRepository,
            edu.upc.dmag.beaconLoaderWithCLI.entities.MeasureRepository measureRepository,
            edu.upc.dmag.beaconLoaderWithCLI.entities.MeasurementValueRepository measurementValueRepository,
            edu.upc.dmag.beaconLoaderWithCLI.entities.ValueRepository valueRepository,
            edu.upc.dmag.beaconLoaderWithCLI.entities.QuantityRepository quantityRepository,
            edu.upc.dmag.beaconLoaderWithCLI.entities.ComplexValueRepository complexValueRepository,
            edu.upc.dmag.beaconLoaderWithCLI.entities.PhenotypicFeatureRepository phenotypicFeatureRepository) {
        this.individualRepository = individualRepository;
        this.ontologyTermRepository = ontologyTermRepository;
        this.measureRepository = measureRepository;
        this.measurementValueRepository = measurementValueRepository;
        this.valueRepository = valueRepository;
        this.quantityRepository = quantityRepository;
        this.complexValueRepository = complexValueRepository;
        this.phenotypicFeatureRepository = phenotypicFeatureRepository;
    }

    @Bean
    public Job loadIndividualsJob(JobRepository jobRepository, Step loadIndividualsStep) {
        return new JobBuilder("loadIndividualsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(loadIndividualsStep)
                .build();
    }

    @Bean
    public Step loadIndividualsStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     ItemReader<IndividualsSchema> individualReader,
                                     ItemProcessor<IndividualsSchema, edu.upc.dmag.beaconLoaderWithCLI.entities.Individual> individualProcessor,
                                     ItemWriter<edu.upc.dmag.beaconLoaderWithCLI.entities.Individual> individualWriter) {
        return new StepBuilder("loadIndividualsStep", jobRepository)
                .<IndividualsSchema, edu.upc.dmag.beaconLoaderWithCLI.entities.Individual>chunk(10, transactionManager)
                .reader(individualReader)
                .processor(individualProcessor)
                .writer(individualWriter)
                .build();
    }

    @Bean
    public ItemReader<IndividualsSchema> individualReader() {
        return new IndividualJsonItemReader("./src/main/resources/toLoad/individuals.json");
    }

    @Bean
    public ItemProcessor<IndividualsSchema, edu.upc.dmag.beaconLoaderWithCLI.entities.Individual> individualProcessor() {
        return readIndividual -> {
            var individual = new Individual();
            individual.setId(readIndividual.getId());
            individual.setMeasures(getMeasures(readIndividual.getMeasures()));
            individual.setPhenotypicFeatures(getPhenotypicFeatures(readIndividual.getPhenotypicFeatures()));
            individual.setSex(getOntologyTerm(readIndividual.getSex()));
            return individual;
        };
    }

    private OntologyTerm getOntologyTerm(Sex sex) {
        var foundTerm = ontologyTermRepository.findById(sex.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(sex.getId());
            ontologyTerm.setLabel(sex.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    @Bean
    public ItemWriter<edu.upc.dmag.beaconLoaderWithCLI.entities.Individual> individualWriter() {
        return individuals -> {
            for (edu.upc.dmag.beaconLoaderWithCLI.entities.Individual individual : individuals) {
                individualRepository.save(individual);
            }
        };
    }

    // Helper methods for processing

    private Set<edu.upc.dmag.beaconLoaderWithCLI.entities.Measure> getMeasures(List<edu.upc.dmag.ToLoad.Measure> measures) {
        return measures.stream().map(this::getMeasure).collect(Collectors.toSet());
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.Measure getMeasure(edu.upc.dmag.ToLoad.Measure it) {
        var measure = new edu.upc.dmag.beaconLoaderWithCLI.entities.Measure();
        measure.setAssayCode(getOntologyTermFromAssayCode(it.getAssayCode()));
        measure.setDate(Date.valueOf(it.getDate()));
        measure.setMeasurementValue(getMeasurementValue(it.getMeasurementValue()));
        measureRepository.save(measure);
        return measure;
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm getOntologyTermFromAssayCode(AssayCode__1 assayCode) {
        var foundTerm = ontologyTermRepository.findById(assayCode.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm ontologyTerm = new edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm();
            ontologyTerm.setId(assayCode.getId());
            ontologyTerm.setLabel(assayCode.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.MeasurementValue getMeasurementValue(edu.upc.dmag.ToLoad.MeasurementValue readMeasurementValue) {
        var measurementValue = new edu.upc.dmag.beaconLoaderWithCLI.entities.MeasurementValue();
        if (readMeasurementValue.getQuantity() != null) {
            var value = new edu.upc.dmag.beaconLoaderWithCLI.entities.Value();
            if (readMeasurementValue.getQuantity().getOntologyTerm() != null) {
                value.setTermValue(getOntologyTermFromOntologyTerm(readMeasurementValue.getQuantity().getOntologyTerm()));
            } else {
                var quantity = getQuantityFromQuantity(readMeasurementValue.getQuantity());
                value.setQuantity(quantity);
            }
            valueRepository.save(value);
            measurementValue.setValue(value);
        } else {
            var complexValue = new edu.upc.dmag.beaconLoaderWithCLI.entities.ComplexValue();
            complexValue.setQuantity(getQuantityFromQuantity1(readMeasurementValue.getTypedQuantities().getQuantity()));
            complexValue.setQuantityType(getOntologyTermFromQuantityType(readMeasurementValue.getTypedQuantities().getQuantityType()));
            complexValueRepository.save(complexValue);
            measurementValue.setComplexValue(complexValue);
        }
        measurementValueRepository.save(measurementValue);
        return measurementValue;
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm getOntologyTermFromOntologyTerm(edu.upc.dmag.ToLoad.OntologyTerm readOntologyTerm) {
        var foundTerm = ontologyTermRepository.findById(readOntologyTerm.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm ontologyTerm = new edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm();
            ontologyTerm.setId(readOntologyTerm.getId());
            ontologyTerm.setLabel(readOntologyTerm.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm getOntologyTermFromQuantityType(QuantityType readType) {
        var foundTerm = ontologyTermRepository.findById(readType.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm ontologyTerm = new edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm();
            ontologyTerm.setId(readType.getId());
            ontologyTerm.setLabel(readType.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.Quantity getQuantityFromQuantity1(Quantity__1 readQuantity) {
        var quantity = new edu.upc.dmag.beaconLoaderWithCLI.entities.Quantity();
        quantity.setValue(readQuantity.getValue());
        quantity.setUnit(getOntologyTermFromUnit3(readQuantity.getUnit()));
        if (readQuantity.getReferenceRange() != null) {
            var referenceRange = new edu.upc.dmag.beaconLoaderWithCLI.entities.ReferenceRange();
            referenceRange.setLow(readQuantity.getReferenceRange().getLow());
            referenceRange.setHigh(readQuantity.getReferenceRange().getHigh());
            referenceRange.setUnit(getOntologyTermFromUnit4(readQuantity.getReferenceRange().getUnit()));
            quantity.setReferenceRange(referenceRange);
        }
        quantityRepository.save(quantity);
        return quantity;
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.Quantity getQuantityFromQuantity(edu.upc.dmag.ToLoad.Quantity readQuantity) {
        var quantity = new edu.upc.dmag.beaconLoaderWithCLI.entities.Quantity();
        quantity.setValue(readQuantity.getValue());
        quantity.setUnit(getOntologyTermFromUnit1(readQuantity.getUnit()));
        if (readQuantity.getReferenceRange() != null) {
            var referenceRange = new edu.upc.dmag.beaconLoaderWithCLI.entities.ReferenceRange();
            referenceRange.setLow(readQuantity.getReferenceRange().getLow());
            referenceRange.setHigh(readQuantity.getReferenceRange().getHigh());
            referenceRange.setUnit(getOntologyTermFromUnit2(readQuantity.getReferenceRange().getUnit()));
            quantity.setReferenceRange(referenceRange);
        }
        quantityRepository.save(quantity);
        return quantity;
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm getOntologyTermFromUnit4(Unit__4 unit) {
        var foundTerm = ontologyTermRepository.findById(unit.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm ontologyTerm = new edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm();
            ontologyTerm.setId(unit.getId());
            ontologyTerm.setLabel(unit.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm getOntologyTermFromUnit3(Unit__3 unit) {
        var foundTerm = ontologyTermRepository.findById(unit.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm ontologyTerm = new edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm();
            ontologyTerm.setId(unit.getId());
            ontologyTerm.setLabel(unit.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm getOntologyTermFromUnit1(Unit__1 unit) {
        var foundTerm = ontologyTermRepository.findById(unit.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm ontologyTerm = new edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm();
            ontologyTerm.setId(unit.getId());
            ontologyTerm.setLabel(unit.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm getOntologyTermFromUnit2(Unit__2 unit) {
        var foundTerm = ontologyTermRepository.findById(unit.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm ontologyTerm = new edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm();
            ontologyTerm.setId(unit.getId());
            ontologyTerm.setLabel(unit.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    private Set<edu.upc.dmag.beaconLoaderWithCLI.entities.PhenotypicFeature> getPhenotypicFeatures(List<PhenotypicFeature__1> phenotypicFeatures) {
        var result = new HashSet<edu.upc.dmag.beaconLoaderWithCLI.entities.PhenotypicFeature>();
        for (var phenotypicFeature : phenotypicFeatures) {
            result.add(getPhenotypicFeature(phenotypicFeature));
        }
        return result;
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.PhenotypicFeature getPhenotypicFeature(PhenotypicFeature__1 readPhenotypicFeature) {
        var phenotypicFeature = new edu.upc.dmag.beaconLoaderWithCLI.entities.PhenotypicFeature();
        phenotypicFeature.setFeatureType(getOntologyTermFromFeatureType(readPhenotypicFeature.getFeatureType()));
        phenotypicFeature.setModifiers(getOntologyTermsModifiers(readPhenotypicFeature.getModifiers()));
        phenotypicFeatureRepository.save(phenotypicFeature);
        return phenotypicFeature;
    }

    private Set<edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm> getOntologyTermsModifiers(List<Modifier__3> modifiers) {
        return modifiers.stream().map(this::getOntologyTermFromModifier).collect(Collectors.toSet());
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm getOntologyTermFromModifier(Modifier__3 modifier__3) {
        var foundTerm = ontologyTermRepository.findById(modifier__3.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm ontologyTerm = new edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm();
            ontologyTerm.setId(modifier__3.getId());
            ontologyTerm.setLabel(modifier__3.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm getOntologyTermFromFeatureType(FeatureType__3 featureType) {
        var foundTerm = ontologyTermRepository.findById(featureType.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm ontologyTerm = new edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm();
            ontologyTerm.setId(featureType.getId());
            ontologyTerm.setLabel(featureType.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm getOntologyTermFromSex(Sex sex) {
        var foundTerm = ontologyTermRepository.findById(sex.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm ontologyTerm = new edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm();
            ontologyTerm.setId(sex.getId());
            ontologyTerm.setLabel(sex.getLabel());
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }

    /**
     * Custom ItemReader that reads IndividualsSchema objects from a JSON array file.
     */
    public static class IndividualJsonItemReader implements ItemReader<IndividualsSchema> {
        private final String filePath;
        private Iterator<IndividualsSchema> individualIterator;
        private boolean initialized = false;

        public IndividualJsonItemReader(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public IndividualsSchema read() {
            if (!initialized) {
                initialize();
            }
            if (individualIterator != null && individualIterator.hasNext()) {
                return individualIterator.next();
            }
            return null;
        }

        private void initialize() {
            try {
                InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream(filePath));
                Gson gson = new Gson();
                IndividualsSchema[] readIndividuals = gson.fromJson(jsonFileInputStream, IndividualsSchema[].class);
                individualIterator = Arrays.asList(readIndividuals).iterator();
                initialized = true;
            } catch (IOException e) {
                throw new RuntimeException("Error reading individuals JSON file: " + filePath, e);
            }
        }
    }
}

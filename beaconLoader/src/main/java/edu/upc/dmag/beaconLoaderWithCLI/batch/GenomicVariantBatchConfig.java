package edu.upc.dmag.beaconLoaderWithCLI.batch;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import edu.upc.dmag.ToLoad.*;
import edu.upc.dmag.ToLoad.ClinicalInterpretation;
import edu.upc.dmag.ToLoad.FrequencyInPopulation;
import edu.upc.dmag.beaconLoaderWithCLI.BeaconLoaderWithCliApplication;
import edu.upc.dmag.beaconLoaderWithCLI.MolecularAttribute;
import edu.upc.dmag.beaconLoaderWithCLI.config.DataLoadPathConfig;
import edu.upc.dmag.beaconLoaderWithCLI.entities.*;
import edu.upc.dmag.beaconLoaderWithCLI.entities.GenomicFeature;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Interval;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Location;
import edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm;
import edu.upc.dmag.beaconLoaderWithCLI.entities.PhenotypicEffect;
import edu.upc.dmag.beaconLoaderWithCLI.entities.VariantAlternativeId;
import edu.upc.dmag.beaconLoaderWithCLI.entities.VariantLevelData;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Variation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Configuration
public class GenomicVariantBatchConfig {
    private static final Logger LOG = LoggerFactory.getLogger(GenomicVariantBatchConfig.class);

    private final GenomicVariationRepository genomicVariationRepository;
    private final VariantAlternativeIdRepository variantAlternativeIdRepository;
    private final PhenotypicEffectRepository phenotypicEffectRepository;
    private final ClinicalInterpretationRepository clinicalInterpretationRepository;
    private final VariantLevelDataRepository variantLevelDataRepository;
    private final LocationRepository locationRepository;
    private final VariationRepository variationRepository;
    private final IntervalRepository intervalRepository;
    private final CaseLevelDataRepository caseLevelDataRepository;
    private final FrequencyInPopulationsRepository frequencyInPopulationsRepository;
    private final FrequencyInPopulationRepository frequencyInPopulationRepository;
    private final MolecularAttributeRepository molecularAttributeRepository;
    private final OntologyTermRepository ontologyTermRepository;
    private final AnalysisRepository analysisRepository;
    private final DataLoadPathConfig dataLoadPathConfig;

    public GenomicVariantBatchConfig(
            GenomicVariationRepository genomicVariationRepository,
            VariantAlternativeIdRepository variantAlternativeIdRepository,
            PhenotypicEffectRepository phenotypicEffectRepository,
            ClinicalInterpretationRepository clinicalInterpretationRepository,
            VariantLevelDataRepository variantLevelDataRepository,
            LocationRepository locationRepository,
            VariationRepository variationRepository,
            IntervalRepository intervalRepository,
            CaseLevelDataRepository caseLevelDataRepository,
            FrequencyInPopulationsRepository frequencyInPopulationsRepository,
            FrequencyInPopulationRepository frequencyInPopulationRepository,
            MolecularAttributeRepository molecularAttributeRepository,
            OntologyTermRepository ontologyTermRepository,
            AnalysisRepository analysisRepository,
            DataLoadPathConfig dataLoadPathConfig) {
        this.genomicVariationRepository = genomicVariationRepository;
        this.variantAlternativeIdRepository = variantAlternativeIdRepository;
        this.phenotypicEffectRepository = phenotypicEffectRepository;
        this.clinicalInterpretationRepository = clinicalInterpretationRepository;
        this.variantLevelDataRepository = variantLevelDataRepository;
        this.locationRepository = locationRepository;
        this.variationRepository = variationRepository;
        this.intervalRepository = intervalRepository;
        this.caseLevelDataRepository = caseLevelDataRepository;
        this.frequencyInPopulationsRepository = frequencyInPopulationsRepository;
        this.frequencyInPopulationRepository = frequencyInPopulationRepository;
        this.molecularAttributeRepository = molecularAttributeRepository;
        this.ontologyTermRepository = ontologyTermRepository;
        this.analysisRepository = analysisRepository;
        this.dataLoadPathConfig = dataLoadPathConfig;
    }

    @Bean
    public Job loadGenomicVariantsJob(JobRepository jobRepository, Step loadGenomicVariantsStep) {
        return new JobBuilder("loadGenomicVariantsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(loadGenomicVariantsStep)
                .build();
    }

    @Bean
    public Step loadGenomicVariantsStep(JobRepository jobRepository,
                                         PlatformTransactionManager transactionManager,
                                         ItemReader<GenomicVariantsSchema> genomicVariantReader,
                                         ItemProcessor<GenomicVariantsSchema, GenomicVariation> genomicVariantProcessor,
                                         ItemWriter<GenomicVariation> genomicVariantWriter) {
        return new StepBuilder("loadGenomicVariantsStep", jobRepository)
                .<GenomicVariantsSchema, GenomicVariation>chunk(10, transactionManager)
                .reader(genomicVariantReader)
                .processor(genomicVariantProcessor)
                .writer(genomicVariantWriter)
                .build();
    }

    @Bean
    public ItemReader<GenomicVariantsSchema> genomicVariantReader() {
        return new GenomicVariantJsonItemReader(dataLoadPathConfig.getGenomicVariationsPath());
    }

    @Bean
    public ItemProcessor<GenomicVariantsSchema, GenomicVariation> genomicVariantProcessor() {
        return readGenomicVariant -> {
            var foundGenomicVariant = genomicVariationRepository.findById(readGenomicVariant.getVariantInternalId());
            if (foundGenomicVariant.isPresent()) {
                GenomicVariation genomicVariation = foundGenomicVariant.get();
                genomicVariation.getCaseLevelData().addAll(getCaseLevelDataList(readGenomicVariant.getCaseLevelData()));
                genomicVariation.getFrequencyInPopulationsList().addAll(getFrequencyInPopulationsList(readGenomicVariant.getFrequencyInPopulations()));
                genomicVariation.getProteinHGVSIds().addAll(readGenomicVariant.getIdentifiers().getProteinHGVSIds());
                genomicVariation.getTranscriptHGVSIds().addAll(readGenomicVariant.getIdentifiers().getTranscriptHGVSIds());
                genomicVariation.getVariantAlternativeIds().addAll(getVariantAlternativeIds(readGenomicVariant.getIdentifiers().getVariantAlternativeIds()));
                processMolecularAttributes(readGenomicVariant, genomicVariation);

                var variantLevelData = getVariantLevelData(readGenomicVariant.getVariantLevelData());
                genomicVariation.getVariantLevelData().getClinicalInterpretations().addAll(variantLevelData.getClinicalInterpretations());
                genomicVariation.getVariantLevelData().getPhenotypicEffects().addAll(variantLevelData.getPhenotypicEffects());

                return genomicVariation;
            }

            GenomicVariation genomicVariation = new GenomicVariation();

            genomicVariation.setVariantInternalId(readGenomicVariant.getVariantInternalId());
            genomicVariation.setCaseLevelData(getCaseLevelDataList(readGenomicVariant.getCaseLevelData()));
            genomicVariation.setFrequencyInPopulationsList(getFrequencyInPopulationsList(readGenomicVariant.getFrequencyInPopulations()));

            processGenomicVariationIdentifier(readGenomicVariant, genomicVariation);
            processMolecularAttributes(readGenomicVariant, genomicVariation);

            genomicVariation.setVariantLevelData(getVariantLevelData(readGenomicVariant.getVariantLevelData()));

            genomicVariation.setVariation(getVariation(readGenomicVariant.getVariation()));

            return genomicVariation;
        };
    }

    @Bean
    public ItemWriter<GenomicVariation> genomicVariantWriter() {
        return genomicVariations -> {
            for (GenomicVariation genomicVariation : genomicVariations) {
                genomicVariationRepository.save(genomicVariation);
            }
        };
    }

    // Helper methods

    private Variation getVariation(edu.upc.dmag.ToLoad.Variation readVariation) {
        var variation = new Variation();
        variation.setVariantType(readVariation.getVariantType());
        variation.setReferenceBases(readVariation.getReferenceBases());
        variation.setAlternateBases(readVariation.getAlternateBases());
        variation.setLocation(getLocation(readVariation.getLocation()));
        variationRepository.save(variation);
        return variation;
    }

    private Location getLocation(Location__2 location) {
        var result = new Location();
        result.setSpecies_id(location.getSpeciesId());
        result.setChr(location.getChr());
        result.setInterval(getInterval(location.getInterval()));
        locationRepository.save(result);
        return result;
    }

    private Interval getInterval(edu.upc.dmag.ToLoad.Interval interval) {
        var result = new Interval();
        result.setType(interval.getType());
        result.setStart(interval.getStart().getValue().toString());
        result.setEnd(interval.getEnd().getValue().toString());
        intervalRepository.save(result);
        return result;
    }

    private VariantLevelData getVariantLevelData(edu.upc.dmag.ToLoad.VariantLevelData variantLevelData) {
        if (variantLevelData == null) {
            return null;
        }
        var result = new VariantLevelData();

        result.setClinicalInterpretations(getClinicalInterpretationsForVariantLevelData(variantLevelData.getClinicalInterpretations()));
        result.setPhenotypicEffects(getPhenotypicEffectsForVariantLevelData(variantLevelData.getPhenotypicEffects()));
        variantLevelDataRepository.save(result);
        return result;
    }

    private List<edu.upc.dmag.beaconLoaderWithCLI.entities.ClinicalInterpretation> getPhenotypicEffectsForVariantLevelData(List<PhenotypicEffect__1> phenotypicEffects) {
        return phenotypicEffects.stream().map(this::getPhenotypicEffectForVariantLevelData).toList();
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.ClinicalInterpretation getPhenotypicEffectForVariantLevelData(PhenotypicEffect__1 it) {
        var phenotypicEffect = new edu.upc.dmag.beaconLoaderWithCLI.entities.ClinicalInterpretation();
        phenotypicEffect.setAnnotatedWithToolName(it.getAnnotatedWith().getToolName());
        phenotypicEffect.setAnnotatedWithToolVersion(it.getAnnotatedWith().getVersion());

        phenotypicEffect.setCategory(getOntologyTerm(it.getCategory()));

        phenotypicEffect.setClinicalRelevance(getClinicalRelevance(it.getClinicalRelevance()));
        phenotypicEffect.setConditionId(it.getConditionId());
        phenotypicEffect.setEffect(getOntologyTerm(it.getEffect()));
        phenotypicEffect.setEvidenceType(getOntologyTerm(it.getEvidenceType()));

        clinicalInterpretationRepository.save(phenotypicEffect);
        return phenotypicEffect;
    }

    private ClinicalInterpretation.ClinicalRelevance getClinicalRelevance(PhenotypicEffect__1.ClinicalRelevance clinicalRelevance) {
        if (clinicalRelevance == null) return null;
        return ClinicalInterpretation.ClinicalRelevance.fromValue(clinicalRelevance.toString());
    }

    private OntologyTerm getOntologyTerm(EvidenceType__3 evidenceType) {
        var foundTerm = ontologyTermRepository.findById(evidenceType.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(evidenceType.getId());
            ontologyTerm.setLabel(evidenceType.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private OntologyTerm getOntologyTerm(Effect__3 effect) {
        var foundTerm = ontologyTermRepository.findById(effect.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(effect.getId());
            ontologyTerm.setLabel(effect.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private OntologyTerm getOntologyTerm(Category__3 category) {
        var foundTerm = ontologyTermRepository.findById(category.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(category.getId());
            ontologyTerm.setLabel(category.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private List<edu.upc.dmag.beaconLoaderWithCLI.entities.ClinicalInterpretation> getClinicalInterpretationsForVariantLevelData(List<ClinicalInterpretation__1> clinicalInterpretations) {
        return clinicalInterpretations.stream().map(this::getClinicalInterpretationsForVariantLevelDatum).toList();
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.ClinicalInterpretation getClinicalInterpretationsForVariantLevelDatum(ClinicalInterpretation__1 it) {
        var result = new edu.upc.dmag.beaconLoaderWithCLI.entities.ClinicalInterpretation();
        result.setAnnotatedWithToolVersion(it.getAnnotatedWith().getVersion());
        result.setAnnotatedWithToolName(it.getAnnotatedWith().getToolName());
        result.setCategory(getOntologyTerm(it.getCategory()));
        result.setConditionId(it.getConditionId());
        result.setEffect(getOntologyTerm(it.getEffect()));
        result.setEvidenceType(getOntologyTerm(it.getEvidenceType()));
        result.setClinicalRelevance(getClinicalRelevance(it.getClinicalRelevance()));
        clinicalInterpretationRepository.saveAndFlush(result);
        return result;
    }

    private ClinicalInterpretation.ClinicalRelevance getClinicalRelevance(ClinicalInterpretation__1.ClinicalRelevance clinicalRelevance) {
        if (clinicalRelevance == null) { return null; }
        return ClinicalInterpretation.ClinicalRelevance.fromValue(clinicalRelevance.toString());
    }

    private OntologyTerm getOntologyTerm(EvidenceType__2 evidenceType) {
        if (evidenceType == null) {
            return null;
        }
        var foundTerm = ontologyTermRepository.findById(evidenceType.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(evidenceType.getId());
            ontologyTerm.setLabel(evidenceType.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private OntologyTerm getOntologyTerm(Effect__2 effect) {
        var foundTerm = ontologyTermRepository.findById(effect.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(effect.getId());
            ontologyTerm.setLabel(effect.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private OntologyTerm getOntologyTerm(Category__2 category) {
        var foundTerm = ontologyTermRepository.findById(category.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(category.getId());
            ontologyTerm.setLabel(category.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private List<FrequencyInPopulations> getFrequencyInPopulationsList(List<FrequencyInPopulation> frequencyInPopulations) {
        return frequencyInPopulations.stream().map(this::getFrequencyInPopulations).toList();
    }

    private FrequencyInPopulations getFrequencyInPopulations(FrequencyInPopulation it) {
        var result = new FrequencyInPopulations();

        result.setSource(it.getSource());
        result.setSourceReference(it.getSourceReference());
        result.setVersion(it.getVersion());
        result.setFrequencies(getFrequencies(it.getFrequencies()));

        frequencyInPopulationsRepository.save(result);

        return result;
    }

    private List<edu.upc.dmag.beaconLoaderWithCLI.entities.FrequencyInPopulation> getFrequencies(List<Frequency> frequencies) {
        return frequencies.stream().map(this::getFrequencyInPopulation).toList();
    }

    private edu.upc.dmag.beaconLoaderWithCLI.entities.FrequencyInPopulation getFrequencyInPopulation(Frequency it) {
        var frequency = new edu.upc.dmag.beaconLoaderWithCLI.entities.FrequencyInPopulation();
        frequency.setFrequency(it.getAlleleFrequency());
        frequency.setPopulation(it.getPopulation());
        frequencyInPopulationRepository.save(frequency);
        return frequency;
    }

    private String populateUsingBioSampleId(String tentativeAnalysisId, CaseLevelData caseLevelData) {
        var tentativeAnalysis = analysisRepository.findById(tentativeAnalysisId);
        if (tentativeAnalysis.isPresent()) {
            caseLevelData.setAnalysis(tentativeAnalysis.get());
            return tentativeAnalysisId;
        } else {
            throw new IllegalArgumentException("No analysis found for id: " + tentativeAnalysisId);
        }
    }

    private List<PhenotypicEffect> getPhenotypicEffects(List<edu.upc.dmag.ToLoad.PhenotypicEffect> phenotypicEffects) {
        return phenotypicEffects.stream().map(this::phenotypicEffect).collect(Collectors.toList());
    }

    private PhenotypicEffect phenotypicEffect(edu.upc.dmag.ToLoad.PhenotypicEffect readPhenotypicEffect) {
        var phenotypicEffect = new PhenotypicEffect();
        phenotypicEffect.setAnnotationToolName(readPhenotypicEffect.getAnnotatedWith().getToolName());
        phenotypicEffect.setAnnotationToolVersion(readPhenotypicEffect.getAnnotatedWith().getVersion());

        phenotypicEffect.setCategory(getOntologyTerm(readPhenotypicEffect.getCategory()));

        phenotypicEffect.setClinicalRelevance(getClinicalRelevance(readPhenotypicEffect.getClinicalRelevance()));
        phenotypicEffect.setConditionId(readPhenotypicEffect.getConditionId());
        phenotypicEffect.setEffect(getOntologyTerm(readPhenotypicEffect.getEffect()));
        phenotypicEffect.setEvidenceType(getOntologyTerm(readPhenotypicEffect.getEvidenceType()));

        phenotypicEffectRepository.save(phenotypicEffect);
        return phenotypicEffect;
    }

    private ClinicalInterpretation.ClinicalRelevance getClinicalRelevance(edu.upc.dmag.ToLoad.PhenotypicEffect.ClinicalRelevance clinicalRelevance) {
        if (clinicalRelevance == null) return null;
        return ClinicalInterpretation.ClinicalRelevance.fromValue(clinicalRelevance.toString());
    }

    private OntologyTerm getOntologyTerm(EvidenceType__1 evidenceType) {
        var foundTerm = ontologyTermRepository.findById(evidenceType.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(evidenceType.getId());
            ontologyTerm.setLabel(evidenceType.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private OntologyTerm getOntologyTerm(Effect__1 effect) {
        var foundTerm = ontologyTermRepository.findById(effect.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(effect.getId());
            ontologyTerm.setLabel(effect.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private OntologyTerm getOntologyTerm(Category__1 category) {
        var foundTerm = ontologyTermRepository.findById(category.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(category.getId());
            ontologyTerm.setLabel(category.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private List<PhenotypicEffect> getClinicalInterpretations(List<ClinicalInterpretation> readClinicalInterpretations) {
        return readClinicalInterpretations.stream().map(this::getClinicalInterpretation).collect(Collectors.toList());
    }

    private PhenotypicEffect getClinicalInterpretation(ClinicalInterpretation readClinicalInterpretation) {
        var phenotypicEffect = new PhenotypicEffect();
        phenotypicEffect.setAnnotationToolName(readClinicalInterpretation.getAnnotatedWith().getToolName());
        phenotypicEffect.setAnnotationToolVersion(readClinicalInterpretation.getAnnotatedWith().getVersion());

        phenotypicEffect.setCategory(getOntologyTerm(readClinicalInterpretation.getCategory()));

        phenotypicEffect.setClinicalRelevance(readClinicalInterpretation.getClinicalRelevance());
        phenotypicEffect.setConditionId(readClinicalInterpretation.getConditionId());
        phenotypicEffect.setEffect(getOntologyTerm(readClinicalInterpretation.getEffect()));
        phenotypicEffect.setEvidenceType(getOntologyTerm(readClinicalInterpretation.getEvidenceType()));

        phenotypicEffectRepository.save(phenotypicEffect);
        return phenotypicEffect;
    }

    private List<CaseLevelData> getCaseLevelDataList(List<CaseLevelDatum> readCaseLevelData) {
        return readCaseLevelData.stream().map(this::getReadCaseLevelData).collect(Collectors.toList());
    }

    private CaseLevelData getReadCaseLevelData(CaseLevelDatum caseLevelDatum) {
        CaseLevelData caseLevelData = new CaseLevelData();
        caseLevelData.setAlleleOrigin(getOntologyTerm(caseLevelDatum.getAlleleOrigin()));
        if (caseLevelDatum.getAnalysisId() != null) {
            caseLevelData.setAnalysis(analysisRepository.getReferenceById(caseLevelDatum.getAnalysisId()));
        }
        caseLevelData.setClinicalInterpretations(getClinicalInterpretations(caseLevelDatum.getClinicalInterpretations()));
        caseLevelData.setPhenotypicEffects(getPhenotypicEffects(caseLevelDatum.getPhenotypicEffects()));
        caseLevelData.setZygosity(getOntologyTerm(caseLevelDatum.getZygosity()));
        caseLevelData.setDepth(caseLevelDatum.getDepth());
        caseLevelDataRepository.save(caseLevelData);
        return caseLevelData;
    }

    private PhenotypicEffect PhenotypicEffect(edu.upc.dmag.ToLoad.PhenotypicEffect readPhenotypicEffect) {
        var phenotypicEffect = new PhenotypicEffect();
        phenotypicEffect.setAnnotationToolName(readPhenotypicEffect.getAnnotatedWith().getToolName());
        phenotypicEffect.setAnnotationToolVersion(readPhenotypicEffect.getAnnotatedWith().getVersion());
        //phenotypicEffect.setAnnotationToolReference(readPhenotypicEffect.getAnnotatedWith().getToolReferences());

        phenotypicEffect.setCategory(getOntologyTerm(readPhenotypicEffect.getCategory()));

        phenotypicEffect.setClinicalRelevance(getClinicalRelevance(readPhenotypicEffect.getClinicalRelevance()));
        phenotypicEffect.setConditionId(readPhenotypicEffect.getConditionId());
        phenotypicEffect.setEffect(getOntologyTerm(readPhenotypicEffect.getEffect()));
        phenotypicEffect.setEvidenceType(getOntologyTerm(readPhenotypicEffect.getEvidenceType()));

        phenotypicEffectRepository.save(phenotypicEffect);
        return phenotypicEffect;
    }

    private OntologyTerm getOntologyTerm(EvidenceType evidenceType) {
        var foundTerm = ontologyTermRepository.findById(evidenceType.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(evidenceType.getId());
            ontologyTerm.setLabel(evidenceType.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private OntologyTerm getOntologyTerm(Effect effect) {
        var foundTerm = ontologyTermRepository.findById(effect.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(effect.getId());
            ontologyTerm.setLabel(effect.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private OntologyTerm getOntologyTerm(Category category) {
        var foundTerm = ontologyTermRepository.findById(category.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(category.getId());
            ontologyTerm.setLabel(category.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private OntologyTerm getOntologyTerm(AlleleOrigin alleleOrigin) {
        if (alleleOrigin == null){
            return null;
        }
        var foundTerm = ontologyTermRepository.findById(alleleOrigin.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(alleleOrigin.getId());
            ontologyTerm.setLabel(alleleOrigin.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private OntologyTerm getOntologyTerm(Zygosity zygosity) {
        String convertedId = zygosity.getId()+"_"+zygosity.getLabel();
        var foundTerm = ontologyTermRepository.findById(convertedId);
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(convertedId);
            ontologyTerm.setLabel(zygosity.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private void processGenomicVariationIdentifier(GenomicVariantsSchema readGenomicVariant, GenomicVariation genomicVariation) {
        genomicVariation.setClinvarVariantId(readGenomicVariant.getIdentifiers().getClinvarVariantId());
        genomicVariation.setGenomicHGVSId(readGenomicVariant.getIdentifiers().getGenomicHGVSId());
        genomicVariation.setProteinHGVSIds(readGenomicVariant.getIdentifiers().getProteinHGVSIds());
        genomicVariation.setTranscriptHGVSIds(readGenomicVariant.getIdentifiers().getTranscriptHGVSIds());
        genomicVariation.setVariantAlternativeIds(getVariantAlternativeIds(readGenomicVariant.getIdentifiers().getVariantAlternativeIds()));
    }

    private List<VariantAlternativeId> getVariantAlternativeIds(List<edu.upc.dmag.ToLoad.VariantAlternativeId> variantAlternativeIds) {
        return variantAlternativeIds.stream().map(this::getVariantAlternativeId).collect(Collectors.toSet()).stream().toList();
    }

    private VariantAlternativeId getVariantAlternativeId(edu.upc.dmag.ToLoad.VariantAlternativeId readVariantAlternativeId) {
        var foundAlternativeId = variantAlternativeIdRepository.findById(readVariantAlternativeId.getId());
        if (foundAlternativeId.isPresent()) {
            return foundAlternativeId.get();
        } else {
            var variantAlternativeId = new VariantAlternativeId();
            variantAlternativeId.setId(readVariantAlternativeId.getId());
            variantAlternativeId.setNotes(readVariantAlternativeId.getNotes());
            variantAlternativeId.setReference(readVariantAlternativeId.getReference());
            variantAlternativeIdRepository.save(variantAlternativeId);
            return variantAlternativeId;
        }
    }

    private void processMolecularAttributes(GenomicVariantsSchema readGenomicVariant, GenomicVariation genomicVariation) {
        List<MolecularAttribute> molecularAttributes = new ArrayList<MolecularAttribute>();
        int numberMolecularAttributes = readGenomicVariant.getMolecularAttributes().getAminoacidChanges().size();
        for(int i=0; i<numberMolecularAttributes; i++) {
            MolecularAttribute molecularAttribute = new MolecularAttribute();
            molecularAttribute.setAminoacidChange(readGenomicVariant.getMolecularAttributes().getAminoacidChanges().get(i));
            molecularAttribute.setGeneId(readGenomicVariant.getMolecularAttributes().getGeneIds().get(i));
            if (readGenomicVariant.getMolecularAttributes().getGenomicFeatures().size() == numberMolecularAttributes) {
                molecularAttribute.setGenomicFeature(getGenomicFeature(readGenomicVariant.getMolecularAttributes().getGenomicFeatures().get(i)));
            }
            molecularAttribute.setMolecularEffect(getOntologyTerm(readGenomicVariant.getMolecularAttributes().getMolecularEffects().get(i)));
            molecularAttribute.setAnnotationImpact(AnnotationImpact.fromString(readGenomicVariant.getMolecularAttributes().getAnnotationImpact().get(i)));
            molecularAttributeRepository.save(molecularAttribute);
            molecularAttributes.add(molecularAttribute);
        }
        genomicVariation.setMolecularAttributes(molecularAttributes);
    }

    private OntologyTerm getOntologyTerm(MolecularEffect it) {
        var foundTerm = ontologyTermRepository.findById(it.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(it.getId());
            ontologyTerm.setLabel(it.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private GenomicFeature getGenomicFeature(edu.upc.dmag.ToLoad.GenomicFeature readGenomicFeature) {
        GenomicFeature genomicFeature = new GenomicFeature();
        genomicFeature.setFeatureId(getOntologyTerm(readGenomicFeature.getFeatureID()));
        genomicFeature.setFeatureClass(getOntologyTerm(readGenomicFeature.getFeatureClass()));

        return genomicFeature;
    }

    private OntologyTerm getOntologyTerm(FeatureClass featureClass) {
        var foundTerm = ontologyTermRepository.findById(featureClass.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(featureClass.getId());
            ontologyTerm.setLabel(featureClass.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    private OntologyTerm getOntologyTerm(FeatureID featureID) {
        var foundTerm = ontologyTermRepository.findById(featureID.getId());
        if (foundTerm.isPresent()) {
            return foundTerm.get();
        } else {
            OntologyTerm ontologyTerm = new OntologyTerm();
            ontologyTerm.setId(featureID.getId());
            ontologyTerm.setLabel(featureID.getLabel());
            ontologyTermRepository.saveAndFlush(ontologyTerm);
            return ontologyTerm;
        }
    }

    /**
     * Custom ItemReader that reads GenomicVariantsSchema objects from a JSON array file.
     * Supports streaming for large files and gzip compression.
     */
    public static class GenomicVariantJsonItemReader implements ItemReader<GenomicVariantsSchema> {
        private final String filePath;
        private JsonReader jsonReader;
        private Gson gson;
        private boolean initialized = false;

        public GenomicVariantJsonItemReader(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public GenomicVariantsSchema read() {
            if (!initialized) {
                initialize();
            }
            try {
                if (jsonReader != null && jsonReader.hasNext()) {
                    return gson.fromJson(jsonReader, GenomicVariantsSchema.class);
                }
                if (jsonReader != null) {
                    jsonReader.endArray();
                    jsonReader.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Error reading genomic variants JSON file: " + filePath, e);
            }
            return null;
        }

        private void initialize() {
            try {
                File inputFile = new File(filePath);
                readJsonFromMaybeCompressedFile(inputFile);
            } catch (IOException e) {
                try {
                    File inputFile = new File(filePath + ".gz");
                    readJsonFromMaybeCompressedFile(inputFile);
                } catch (IOException e2) {
                    throw new RuntimeException("Error initializing genomic variants JSON file: " + filePath + " or " + filePath + ".gz", e);
                }
            }
        }

        private void readJsonFromMaybeCompressedFile(File inputFile) throws IOException {
            InputStream inputStream = new FileInputStream(inputFile);
            InputStream decompressedStream = maybeDecompress(inputStream, inputFile.getName());
            InputStreamReader jsonFileReader = new InputStreamReader(decompressedStream);
            jsonReader = new JsonReader(jsonFileReader);
            jsonReader.beginArray();
            gson = new Gson();
            initialized = true;
        }

        private static InputStream maybeDecompress(InputStream inputStream, String fileName) throws IOException {
            if (fileName.endsWith(".gz")) {
                return new GZIPInputStream(inputStream);
            }
            return inputStream;
        }
    }
}


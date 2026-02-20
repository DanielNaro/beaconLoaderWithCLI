package edu.upc.dmag.beaconLoaderWithCLI.batch;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import edu.upc.dmag.ToLoad.*;
import edu.upc.dmag.ToLoad.ClinicalInterpretation;
import edu.upc.dmag.ToLoad.FrequencyInPopulation;
import edu.upc.dmag.beaconLoaderWithCLI.MolecularAttribute;
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

    private final Map<String, String> biosampleIdToAnalysisId = new HashMap<>();
    private final Map<String, String> biosampleRenamers;

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
            AnalysisRepository analysisRepository) {
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
        this.biosampleRenamers = getBiosampleRenamer();
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
        return new GenomicVariantJsonItemReader("./src/main/resources/toLoad/genomicVariationsVcf.json");
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
            ontologyTermRepository.save(ontologyTerm);
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
            ontologyTermRepository.save(ontologyTerm);
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
            ontologyTermRepository.save(ontologyTerm);
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
        clinicalInterpretationRepository.save(result);
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
            ontologyTermRepository.save(ontologyTerm);
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
            ontologyTermRepository.save(ontologyTerm);
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
            ontologyTermRepository.save(ontologyTerm);
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

    @Nullable
    private String populateUsingBioSampleId(String biosampleId, CaseLevelData caseLevelData) {
        var tentativeAnalysisId = biosampleIdToAnalysisId.get(biosampleId);
        if (tentativeAnalysisId != null) {
            var tentativeAnalysis = analysisRepository.findById(tentativeAnalysisId);
            if (tentativeAnalysis.isPresent()) {
                caseLevelData.setAnalysis(tentativeAnalysis.get());
                return tentativeAnalysisId;
            } else {
                throw new IllegalArgumentException("No analysis found for id: " + tentativeAnalysisId);
            }
        }
        throw new IllegalArgumentException("No analysisId found for id: " + biosampleId);
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
            ontologyTermRepository.save(ontologyTerm);
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
            ontologyTermRepository.save(ontologyTerm);
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
            ontologyTermRepository.save(ontologyTerm);
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
        } else {
            String biosampleId = caseLevelDatum.getBiosampleId();
            try {
                var tentativeAnalysisId = populateUsingBioSampleId(biosampleId, caseLevelData);
            }catch (IllegalArgumentException e) {
                String renamedBiosampleId = biosampleRenamers.get(biosampleId);
                try {
                    var tentativeAnalysisId = populateUsingBioSampleId(renamedBiosampleId, caseLevelData);
                }catch (IllegalArgumentException e2) {
                    System.err.println("bypassing analysis for biosampleid: "+biosampleId);
                }
            }
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
            ontologyTermRepository.save(ontologyTerm);
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
            ontologyTermRepository.save(ontologyTerm);
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
            ontologyTermRepository.save(ontologyTerm);
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
            ontologyTermRepository.save(ontologyTerm);
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
            ontologyTermRepository.save(ontologyTerm);
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
            ontologyTermRepository.save(ontologyTerm);
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
            ontologyTermRepository.save(ontologyTerm);
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
            ontologyTermRepository.save(ontologyTerm);
            return ontologyTerm;
        }
    }


    private static Map<String, String> getBiosampleRenamer() {
        Map<String, String> biosampleRenamers = new HashMap<>();

        biosampleRenamers.put("EU122", "MB21573");
        biosampleRenamers.put("EU130", "MB20184");
        biosampleRenamers.put("EU138", "MB20198");
        biosampleRenamers.put("EU142", "MB21572");
        biosampleRenamers.put("EU156", "MB20195");
        biosampleRenamers.put("EU174", "MB20187");
        biosampleRenamers.put("EU18", "MB20188");
        biosampleRenamers.put("EU181", "MB20192");
        biosampleRenamers.put("EU247", "MB20776");
        biosampleRenamers.put("EU255_UN1929_", "MB20099");
        biosampleRenamers.put("EU266", "MB20193");
        biosampleRenamers.put("EU3", "MB20197");
        biosampleRenamers.put("EU32", "MB20178");
        biosampleRenamers.put("EU323", "MB20179");
        biosampleRenamers.put("EU352_UN2451_", "MB21559");
        biosampleRenamers.put("EU38", "MB20181");
        biosampleRenamers.put("EU389", "MB20185");
        biosampleRenamers.put("EU4", "MB20190");
        biosampleRenamers.put("EU431", "MB20686");
        biosampleRenamers.put("EU45", "MB20180");
        biosampleRenamers.put("EU453", "MB20460");
        biosampleRenamers.put("EU49", "MB21568");
        biosampleRenamers.put("EU492_15_09_20", "MB20366");
        biosampleRenamers.put("EU57", "MB20787");
        biosampleRenamers.put("EU74_UN289_", "MB20097");
        biosampleRenamers.put("EU80", "MB20191");
        biosampleRenamers.put("EU9", "MB20182");
        biosampleRenamers.put("EU94", "MB20194");
        biosampleRenamers.put("PUA_366_S_1", "MB20367");
        biosampleRenamers.put("PUA_422_S_1", "MB20368");
        biosampleRenamers.put("PUA_435_S_2", "MB20369");
        biosampleRenamers.put("PUA_440_S_2", "MB20370");
        biosampleRenamers.put("PUA_448_S_2", "MB20371");
        biosampleRenamers.put("PUA_466_S_2", "MB20373");
        biosampleRenamers.put("PUA_467_S_2", "MB20374");
        biosampleRenamers.put("PUA_498_S_2", "MB20375");
        biosampleRenamers.put("PUA_508_S_1", "MB20376");
        biosampleRenamers.put("PUA_510_S_1", "MB20377");
        biosampleRenamers.put("PUA_554_S_1", "MB20378");
        biosampleRenamers.put("PUA_614_S_2", "MB20379");
        biosampleRenamers.put("PUA_621_S_2", "MB20380");
        biosampleRenamers.put("PUA_650_S_2", "MB20356");
        biosampleRenamers.put("PUA_666_S_2", "MB20357");
        biosampleRenamers.put("PUA_674_S_1", "MB20358");
        biosampleRenamers.put("PUA_678_S_1", "MB20359");
        biosampleRenamers.put("PUA_679_S_1", "MB20360");
        biosampleRenamers.put("PUA_682_S_1", "MB20361");
        biosampleRenamers.put("PUA_728_S_2", "MB20362");
        biosampleRenamers.put("PUA_731_S_1", "MB20363");
        biosampleRenamers.put("PUA_754_S_2", "MB20364");
        biosampleRenamers.put("UN105", "MB20549");
        biosampleRenamers.put("UN1066", "MB20249");
        biosampleRenamers.put("UN1121", "MB20247");
        biosampleRenamers.put("UN1138", "MB21571");
        biosampleRenamers.put("UN1175", "MB20688");
        biosampleRenamers.put("UN128", "MB20245");
        biosampleRenamers.put("UN1310", "MB20218");
        biosampleRenamers.put("UN1341", "MB20219");
        biosampleRenamers.put("UN1352", "MB20535");
        biosampleRenamers.put("UN1471", "MB20224");
        biosampleRenamers.put("UN1533", "MB20726");
        biosampleRenamers.put("UN1557", "MB20410");
        biosampleRenamers.put("UN1559", "MB20212");
        biosampleRenamers.put("UN1613", "MB20207");
        biosampleRenamers.put("UN1688", "MB20199");
        biosampleRenamers.put("UN1689", "MB20215");
        biosampleRenamers.put("UN1692", "MB20223");
        biosampleRenamers.put("UN1706", "MB20489");
        biosampleRenamers.put("UN1774", "MB21567");
        biosampleRenamers.put("UN18", "MB20437");
        biosampleRenamers.put("UN1892", "MB20747");
        biosampleRenamers.put("UN1893", "MB20206");
        biosampleRenamers.put("UN1910", "MB20211");
        biosampleRenamers.put("UN1983", "MB20438");
        biosampleRenamers.put("UN2024", "MB20202");
        biosampleRenamers.put("UN2033", "MB20753");
        biosampleRenamers.put("UN2056", "MB20237");
        biosampleRenamers.put("UN2065", "MB20687");
        biosampleRenamers.put("UN2140", "MB20242");
        biosampleRenamers.put("UN2189", "MB20226");
        biosampleRenamers.put("UN2204", "MB20241");
        biosampleRenamers.put("UN2212", "MB20214");
        biosampleRenamers.put("UN2237", "MB20730");
        biosampleRenamers.put("UN2251", "MB20511");
        biosampleRenamers.put("UN2286", "MB20250");
        biosampleRenamers.put("UN2309_EU207_", "MB20737");
        biosampleRenamers.put("UN2376", "MB20748");
        biosampleRenamers.put("UN2408", "MB20253");
        biosampleRenamers.put("UN2457", "MB20796");
        biosampleRenamers.put("UN2479", "MB20248");
        biosampleRenamers.put("UN2515", "MB20217");
        biosampleRenamers.put("UN2602", "MB20208");
        biosampleRenamers.put("UN2622", "MB20209");
        biosampleRenamers.put("UN2666", "MB20238");
        biosampleRenamers.put("UN2676", "MB20240");
        biosampleRenamers.put("UN2693", "MB20231");
        biosampleRenamers.put("UN2720", "MB20234");
        biosampleRenamers.put("UN2742", "MB20436");
        biosampleRenamers.put("UN2789", "MB20243");
        biosampleRenamers.put("UN2821", "MB20236");
        biosampleRenamers.put("UN2881", "MB20235");
        biosampleRenamers.put("UN2988", "MB20204");
        biosampleRenamers.put("UN2989", "MB20201");
        biosampleRenamers.put("UN3001", "MB20225");
        biosampleRenamers.put("UN3003", "MB20252");
        biosampleRenamers.put("UN3035", "MB20232");
        biosampleRenamers.put("UN3051", "MB21570");
        biosampleRenamers.put("UN3058", "MB20216");
        biosampleRenamers.put("UN3110", "MB20239");
        biosampleRenamers.put("UN3113", "MB20229");
        biosampleRenamers.put("UN3126", "MB20228");
        biosampleRenamers.put("UN3161", "MB20200");
        biosampleRenamers.put("UN3191", "MB20246");
        biosampleRenamers.put("UN3222", "MB20233");
        biosampleRenamers.put("UN3226", "MB20244");
        biosampleRenamers.put("UN3282", "MB20221");
        biosampleRenamers.put("UN3331_EU377_", "MB20735");
        biosampleRenamers.put("UN426", "MB20196");
        biosampleRenamers.put("UN437", "MB20227");
        biosampleRenamers.put("UN547_EU237_", "MB20734");
        biosampleRenamers.put("UN551", "MB20203");
        biosampleRenamers.put("UN602", "MB20210");
        biosampleRenamers.put("UN605", "MB20230");
        biosampleRenamers.put("UN702", "MB21569");
        biosampleRenamers.put("UN75", "MB20205");
        biosampleRenamers.put("UN775", "MB20213");
        biosampleRenamers.put("UN850", "MB20251");
        biosampleRenamers.put("UN851", "MB20689");
        biosampleRenamers.put("UN906", "MB20512");
        biosampleRenamers.put("UN917", "MB20472");
        biosampleRenamers.put("UN930", "MB20480");
        biosampleRenamers.put("UN942", "MB20220");
        return biosampleRenamers;
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
                InputStream inputStream = new FileInputStream(inputFile);
                InputStream decompressedStream = maybeDecompress(inputStream, inputFile.getName());
                InputStreamReader jsonFileReader = new InputStreamReader(decompressedStream);
                jsonReader = new JsonReader(jsonFileReader);
                jsonReader.beginArray();
                gson = new Gson();
                initialized = true;
            } catch (IOException e) {
                throw new RuntimeException("Error initializing genomic variants JSON file: " + filePath, e);
            }
        }

        private static InputStream maybeDecompress(InputStream inputStream, String fileName) throws IOException {
            if (fileName.endsWith(".gz")) {
                return new GZIPInputStream(inputStream);
            }
            return inputStream;
        }
    }
}


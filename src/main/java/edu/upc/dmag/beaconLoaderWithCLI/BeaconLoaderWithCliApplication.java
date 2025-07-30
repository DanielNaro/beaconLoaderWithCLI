package edu.upc.dmag.beaconLoaderWithCLI;


import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import edu.upc.dmag.ToLoad.*;
import edu.upc.dmag.ToLoad.AgeRange;
import edu.upc.dmag.ToLoad.ClinicalInterpretation;
import edu.upc.dmag.ToLoad.DuoDataUse;
import edu.upc.dmag.ToLoad.FrequencyInPopulation;
import edu.upc.dmag.ToLoad.ObtentionProcedure;
import edu.upc.dmag.beaconLoaderWithCLI.entities.*;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Age;
import edu.upc.dmag.beaconLoaderWithCLI.entities.ComplexValue;
import edu.upc.dmag.beaconLoaderWithCLI.entities.DataUseConditions;
import edu.upc.dmag.beaconLoaderWithCLI.entities.GenomicFeature;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Interval;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Location;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Measure;
import edu.upc.dmag.beaconLoaderWithCLI.entities.MeasurementValue;
import edu.upc.dmag.beaconLoaderWithCLI.entities.OntologyTerm;
import edu.upc.dmag.beaconLoaderWithCLI.entities.PhenotypicEffect;
import edu.upc.dmag.beaconLoaderWithCLI.entities.PhenotypicFeature;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Quantity;
import edu.upc.dmag.beaconLoaderWithCLI.entities.ReferenceRange;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Value;
import edu.upc.dmag.beaconLoaderWithCLI.entities.VariantAlternativeId;
import edu.upc.dmag.beaconLoaderWithCLI.entities.VariantLevelData;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Variation;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.sql.Date;
import java.text.ParseException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@SpringBootApplication
public class BeaconLoaderWithCliApplication implements CommandLineRunner {

	private static Logger LOG = LoggerFactory
			.getLogger(BeaconLoaderWithCliApplication.class);
	private final DatasetRepository datasetRepository;
	private final BiosampleRepository biosampleRepository;
	private final OntologyTermRepository ontologyTermRepository;
	private final ObtentionProcedureRepository obtentionProcedureRepository;
	private final MeasureRepository measureRepository;
	private final IndividualRepository individualRepository;
	private final LibrarySelectionRepository librarySelectionRepository;
	private final RunRepository runRepository;
	private final AnalysisRepository analysisRepository;
	private final AgeRangeCriteriaRepository ageRangeCriteriaRepository;
	private final CohortRepository cohortRepository;
	private final PhenotypicFeatureRepository phenotypicFeatureRepository;
	private final DataUseConditionsRepository dataUseConditionsRepository;
	private final AgeRepository ageRepository;
	private final MeasurementValueRepository measurementValueRepository;
	private final ComplexValueRepository complexValueRepository;
	private final ReferenceRangeRepository referenceRangeRepository;
	private final QuantityRepository quantityRepository;
	private final ValueRepository valueRepository;
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
	private final Map<String, String> biosampleIdToAnalysisId = new HashMap<>();
	private final Map<String, String> biosampleRenamers = BeaconLoaderWithCliApplication.getBiosampleRenamer();

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


	public BeaconLoaderWithCliApplication(
			DatasetRepository datasetRepository,
			BiosampleRepository biosampleRepository,
			OntologyTermRepository ontologyTermRepository,
			ObtentionProcedureRepository obtentionProcedureRepository,
			MeasureRepository measureRepository,
			IndividualRepository individualRepository,
			LibrarySelectionRepository librarySelectionRepository,
			RunRepository runRepository,
			AnalysisRepository analysisRepository,
			AgeRangeCriteriaRepository ageRangeCriteriaRepository,
			CohortRepository cohortRepository,
			PhenotypicFeatureRepository phenotypicFeatureRepository,
			DataUseConditionsRepository dataUseConditionsRepository,
			AgeRepository ageRepository,
			MeasurementValueRepository measurementValueRepository,
			ComplexValueRepository complexValueRepository,
			ReferenceRangeRepository referenceRangeRepository,
			QuantityRepository quantityRepository,
			ValueRepository valueRepository,
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
			MolecularAttributeRepository molecularAttributeRepository) {
		this.datasetRepository = datasetRepository;
		this.biosampleRepository = biosampleRepository;
		this.ontologyTermRepository = ontologyTermRepository;
		this.obtentionProcedureRepository = obtentionProcedureRepository;
		this.measureRepository = measureRepository;
		this.individualRepository = individualRepository;
		this.librarySelectionRepository = librarySelectionRepository;
		this.runRepository = runRepository;
		this.analysisRepository = analysisRepository;
		this.ageRangeCriteriaRepository = ageRangeCriteriaRepository;
		this.cohortRepository = cohortRepository;
		this.phenotypicFeatureRepository = phenotypicFeatureRepository;
		this.dataUseConditionsRepository = dataUseConditionsRepository;
		this.ageRepository = ageRepository;
		this.measurementValueRepository = measurementValueRepository;
		this.complexValueRepository = complexValueRepository;
		this.referenceRangeRepository = referenceRangeRepository;
		this.quantityRepository = quantityRepository;
		this.valueRepository = valueRepository;
		this.genomicVariationRepository = genomicVariationRepository;
		this.variantAlternativeIdRepository = variantAlternativeIdRepository;
		this.phenotypicEffectRepository = phenotypicEffectRepository;
		this.clinicalInterpretationRepository = clinicalInterpretationRepository;
		this.variationRepository = variationRepository;
		this.intervalRepository = intervalRepository;
		this.variantLevelDataRepository = variantLevelDataRepository;
		this.locationRepository = locationRepository;
		this.caseLevelDataRepository = caseLevelDataRepository;
		this.frequencyInPopulationsRepository = frequencyInPopulationsRepository;
		this.frequencyInPopulationRepository = frequencyInPopulationRepository;
		this.molecularAttributeRepository = molecularAttributeRepository;
	}

	public static void main(String[] args) {
		LOG.info("STARTING THE APPLICATION");
		SpringApplication.run(BeaconLoaderWithCliApplication.class, args);
		LOG.info("APPLICATION FINISHED");
	}

	@Override
	public void run(String... args) throws IOException {
		LOG.info("EXECUTING : command line runner");

		deleteAll();
		loadDatasets();
		loadIndividuals();
		loadBiosamples();
		loadRuns();
		loadAnalyses();
		loadCohorts();
		loadGenomicVariations();
	}

	private void deleteAll() {
		System.out.println("deleting all");
		datasetRepository.deleteAll();
		biosampleRepository.deleteAll();
		ontologyTermRepository.deleteAll();
		obtentionProcedureRepository.deleteAll();
		measureRepository.deleteAll();
		individualRepository.deleteAll();
		librarySelectionRepository.deleteAll();
		runRepository.deleteAll();
		analysisRepository.deleteAll();
		ageRangeCriteriaRepository.deleteAll();
		cohortRepository.deleteAll();
		phenotypicFeatureRepository.deleteAll();
		dataUseConditionsRepository.deleteAll();
		ageRepository.deleteAll();
		measurementValueRepository.deleteAll();
		complexValueRepository.deleteAll();
		referenceRangeRepository.deleteAll();
		quantityRepository.deleteAll();
		valueRepository.deleteAll();
		genomicVariationRepository.deleteAll();
		variantAlternativeIdRepository.deleteAll();
		phenotypicEffectRepository.deleteAll();
		clinicalInterpretationRepository.deleteAll();
		variationRepository.deleteAll();
		intervalRepository.deleteAll();
		variantLevelDataRepository.deleteAll();
		locationRepository.deleteAll();
		caseLevelDataRepository.deleteAll();
		frequencyInPopulationsRepository.deleteAll();
		frequencyInPopulationRepository.deleteAll();
		System.out.println("deleted all");
	}

	private void loadGenomicVariations() throws IOException {
		Gson gson = new Gson();

		File inputFile = new File("./src/main/resources/toLoad/genomicVariationsVcf.json.gz");
		try (
				InputStream inputStream = new FileInputStream(inputFile);
				InputStream decompressedStream = maybeDecompress(inputStream, inputFile.getName());
				InputStreamReader jsonFileReader = new InputStreamReader(decompressedStream);
				JsonReader reader = new JsonReader(jsonFileReader)
		){
			reader.beginArray();

			while (reader.hasNext()) {
				GenomicVariantsSchema genomicVariant = gson.fromJson(reader, GenomicVariantsSchema.class);
				loadGenomicVariants(genomicVariant);
			}

			reader.endArray();
		}
	}

	private static InputStream maybeDecompress(InputStream inputStream, String fileName) throws IOException {
		if (fileName.endsWith(".gz")) {
			return new GZIPInputStream(inputStream);
		}
		return inputStream; // no compression
	}

	private void loadGenomicVariants(GenomicVariantsSchema readGenomicVariant) {
		var foundGenomicVariant = genomicVariationRepository.findById(readGenomicVariant.getVariantInternalId());
		if (foundGenomicVariant.isPresent()){
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

			genomicVariationRepository.save(genomicVariation);
			return;
		}
		GenomicVariation genomicVariation = new GenomicVariation();

		genomicVariation.setVariantInternalId(readGenomicVariant.getVariantInternalId());
		genomicVariation.setCaseLevelData(getCaseLevelDataList(readGenomicVariant.getCaseLevelData()));
		genomicVariation.setFrequencyInPopulationsList(getFrequencyInPopulationsList(readGenomicVariant.getFrequencyInPopulations()));

		processGenomicVariationIdentifier(readGenomicVariant, genomicVariation);
		processMolecularAttributes(readGenomicVariant, genomicVariation);

		genomicVariation.setVariantLevelData(getVariantLevelData(readGenomicVariant.getVariantLevelData()));

		genomicVariation.setVariation(getVariation(readGenomicVariant.getVariation()));

		genomicVariationRepository.save(genomicVariation);
	}

	private Variation getVariation(edu.upc.dmag.ToLoad.Variation readVariation) {
		var variation = new Variation();
		variation.setVariantType(readVariation.getVariantType());
		variation.setReferenceBases(readVariation.getReferenceBases());
		variation.setAlternateBases(readVariation.getAlternateBases());
		variation.setLocation(getLocation(readVariation.getLocation()));
		//variation.setCopies(getCopies(readVariation.ge));
		//variation.setMembers(getMembers(readVariation.g));
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
		if (variantLevelData == null){
			return null;
		}
		var result = new VariantLevelData();

		result.setClinicalInterpretations(getClinicalInterpretationsForVariantLevelData(variantLevelData.getClinicalInterpretations()));
		result.setPhenotypicEffects(getPhenotypicEffectsForVariantLevelData(variantLevelData.getPhenotypicEffects()));
		variantLevelDataRepository.save(result);
		return result;
	}

	private List<edu.upc.dmag.beaconLoaderWithCLI.entities.ClinicalInterpretation> getPhenotypicEffectsForVariantLevelData(List<PhenotypicEffect__1> phenotypicEffects) {
		return phenotypicEffects.stream().map(it -> getPhenotypicEffectForVariantLevelData(it)).toList();
	}

	private edu.upc.dmag.beaconLoaderWithCLI.entities.ClinicalInterpretation getPhenotypicEffectForVariantLevelData(PhenotypicEffect__1 it) {
		var phenotypicEffect = new edu.upc.dmag.beaconLoaderWithCLI.entities.ClinicalInterpretation();
		phenotypicEffect.setAnnotatedWithToolName(it.getAnnotatedWith().getToolName());
		phenotypicEffect.setAnnotatedWithToolVersion(it.getAnnotatedWith().getVersion());
		//phenotypicEffect.setAnnotationToolReference(it.getAnnotatedWith().getToolReferences());

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
		return clinicalInterpretations.stream().map(it -> getClinicalInterpretationsForVariantLevelDatum(it)).toList();
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

	private PhenotypicEffect getClinicalInterpretation(ClinicalInterpretation__1 it) {
		var phenotypicEffect = new PhenotypicEffect();
		phenotypicEffect.setAnnotationToolName(it.getAnnotatedWith().getToolName());
		phenotypicEffect.setAnnotationToolVersion(it.getAnnotatedWith().getVersion());
		//phenotypicEffect.setAnnotationToolReference(it.getAnnotatedWith().getToolReferences());

		phenotypicEffect.setCategory(getOntologyTerm(it.getCategory()));

		phenotypicEffect.setClinicalRelevance(getClinicalRelevance(it.getClinicalRelevance()));
		phenotypicEffect.setConditionId(it.getConditionId());
		phenotypicEffect.setEffect(getOntologyTerm(it.getEffect()));
		phenotypicEffect.setEvidenceType(getOntologyTerm(it.getEvidenceType()));

		phenotypicEffectRepository.save(phenotypicEffect);
		return phenotypicEffect;
	}

	private ClinicalInterpretation.ClinicalRelevance getClinicalRelevance(ClinicalInterpretation__1.ClinicalRelevance clinicalRelevance) {
		if (clinicalRelevance == null) { return null; }
		return ClinicalInterpretation.ClinicalRelevance.fromValue(clinicalRelevance.toString());
	}

	private OntologyTerm getOntologyTerm(EvidenceType__2 evidenceType) {
		if (evidenceType == null){
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
		return frequencyInPopulations.stream().map(it -> getFrequencyInPopulations(it)).toList();
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
		return phenotypicEffects.stream().map(this::PhenotypicEffect).collect(Collectors.toList());
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
		//phenotypicEffect.setAnnotationToolReference(readClinicalInterpretation.getAnnotatedWith().getToolReferences());

		phenotypicEffect.setCategory(getOntologyTerm(readClinicalInterpretation.getCategory()));

		phenotypicEffect.setClinicalRelevance(readClinicalInterpretation.getClinicalRelevance());
		phenotypicEffect.setConditionId(readClinicalInterpretation.getConditionId());
		phenotypicEffect.setEffect(getOntologyTerm(readClinicalInterpretation.getEffect()));
		phenotypicEffect.setEvidenceType(getOntologyTerm(readClinicalInterpretation.getEvidenceType()));

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

	private List<AnnotationImpact> getAnnotationImpact(List<String> annotationImpact) {
		return annotationImpact.stream().map(AnnotationImpact::fromString).collect(Collectors.toList());
	}

	private AnnotationImpact getMaxAnnotationImpact(List<AnnotationImpact> annotationImpacts) {
		if (annotationImpacts == null || annotationImpacts.isEmpty()) {
			return null;
		}
		AnnotationImpact maxImpact = AnnotationImpact.LOW;
		for (AnnotationImpact impact : annotationImpacts) {
			if (maxImpact.compareTo(impact) < 0) {
				maxImpact = impact;
			}
		}
		return maxImpact;
	}

	private List<OntologyTerm> getMolecularEffects(List<MolecularEffect> molecularEffects) {
		return molecularEffects.stream().map(this::getOntologyTerm).toList();
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


	private List<GenomicFeature> getGenomicFeatures(List<edu.upc.dmag.ToLoad.GenomicFeature> genomicFeatures) {
		return genomicFeatures.stream().map(this::getGenomicFeature).toList();
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

	private void loadCohorts() throws IOException {
		try (InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream("./src/main/resources/toLoad/cohorts.json"))){
			Gson gson = new Gson();
			var readCohorts = gson.fromJson(jsonFileInputStream, CohortsSchema[].class);


			for(CohortsSchema readCohort: readCohorts){
				loadReadCohort(readCohort);
			}
		}
    }

	private void loadReadCohort(CohortsSchema readCohort) {
		Cohort cohort = new Cohort();
		cohort.setId(readCohort.getId());
		cohort.setName(readCohort.getName());
		cohort.setCohortDesign(getOntologyTerm(readCohort.getCohortDesign()));
		cohort.setCohortType(readCohort.getCohortType());
		cohort.setGendersInclusionCriteria(getOntologyTermsGenderInclusion(readCohort.getInclusionCriteria().getGenders()));
		cohort.setAgeRangeInclusionCriteria(getAgeRange(readCohort.getInclusionCriteria().getAgeRange()));
		cohortRepository.save(cohort);
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

	private void loadDatasets() throws IOException {
		try (InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream("./src/main/resources/toLoad/datasets.json"))){
			Gson gson = new Gson();
			var readDatasets = gson.fromJson(jsonFileInputStream, DatasetsSchema[].class);


			for(DatasetsSchema readDataset: readDatasets){
				loadReadDataset(readDataset);
			}
		} catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

	private void loadRuns() throws IOException {
		try (InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream("./src/main/resources/toLoad/runs.json"))){
			Gson gson = new Gson();
			var readRuns = gson.fromJson(jsonFileInputStream, RunsSchema[].class);


			for(RunsSchema readRun: readRuns){
				loadReadRun(readRun);
			}
		}
    }

	private void loadAnalyses() throws IOException {
		try (InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream("./src/main/resources/toLoad/analyses.json"))){
			Gson gson = new Gson();
			var readAnalyses = gson.fromJson(jsonFileInputStream, AnalysesSchema[].class);


			for(AnalysesSchema readAnalysis: readAnalyses){
				loadReadAnalysis(readAnalysis);
			}
		}
	}

	private void loadReadAnalysis(AnalysesSchema readAnalysis) {
		Analysis analysis = new Analysis();
		analysis.setId(readAnalysis.getId());
		analysis.setAligner(readAnalysis.getAligner());
		analysis.setAnalysisDate(Date.valueOf(readAnalysis.getAnalysisDate()));
		analysis.setPipelineName(readAnalysis.getPipelineName());
		analysis.setPipelineRef(readAnalysis.getPipelineRef());
		analysis.setVariantCaller(readAnalysis.getVariantCaller());
		analysis.setRun(runRepository.getReferenceById(readAnalysis.getRunId()));
		analysisRepository.save(analysis);
	}

	private void loadReadRun(RunsSchema readRun) {
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
		runRepository.save(run);
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

	private void loadReadDataset(DatasetsSchema readDataset) throws ParseException {
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

		//readDataset.getDataUseConditions()
		//readDataset.getAdditionalProperties()
		//readDataset.getInfo()

		//readDataset.getInfo().getAdditionalProperties();
		//dataset.getInfo_beacon_contact()
		//dataset.setInfo_beacon_contact(readDataset.getInfo().ge);
		String info_beacon_contact;
		String info_beacon_mapping;
		String info_beacon_version;
		datasetRepository.save(dataset);
	}

	private DataUseConditions getDatauseConditions(edu.upc.dmag.ToLoad.DataUseConditions readDataUseConditions) {
		var dataUseConditions = new DataUseConditions();
		dataUseConditions.setDuoDataUses(
				readDataUseConditions.getDuoDataUse().stream().map(it -> getOntologyTerm(it)).collect(Collectors.toSet())
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

	private void loadBiosamples() throws IOException {

		try (InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream("./src/main/resources/toLoad/biosamples.json"))){
			Gson gson = new Gson();
			var readBiosamples = gson.fromJson(jsonFileInputStream, BiosamplesSchema[].class);


			for(BiosamplesSchema readBiosample: readBiosamples){
				System.out.println(readBiosample.getId()+"\t"+readBiosample.getIndividualId());
				biosampleIdToAnalysisId.put(readBiosample.getId(), readBiosample.getIndividualId());
				loadReadBiosample(readBiosample);
			}
		}
    }

	private void loadIndividuals() throws IOException {
		try (InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream("./src/main/resources/toLoad/individuals.json"))){
			Gson gson = new Gson();
			var readIndividuals = gson.fromJson(jsonFileInputStream, IndividualsSchema[].class);


			for(IndividualsSchema readIndividual: readIndividuals){
				loadReadIndividual(readIndividual);
			}
		}
	}

	private void loadReadIndividual(IndividualsSchema readIndividual) {
		var individual = new Individual();
		individual.setId(readIndividual.getId());
		//individual.setDiseases
		//individual.setEthnicity
		//individual.setExposures
		//individual.setGeographicOrigin
		//individual.setInfo
		//individual.setInterventionsOrProcedures
		//individual.setKaryotypicSex
		individual.setMeasures(getMeasures(readIndividual.getMeasures()));
		//individual.setPedigrees
		individual.setPhenotypicFeatures(getPhenotypicFeatures(readIndividual.getPhenotypicFeatures()));
		individual.setSex(getOntologyTerm(readIndividual.getSex()));
		//individual.setTreatment



		individualRepository.save(individual);
	}

	private Set<Measure> getMeasures(List<edu.upc.dmag.ToLoad.Measure> measures) {
		return measures.stream().map(it -> getMeasure(it)).collect(Collectors.toSet());
	}

	private Measure getMeasure(edu.upc.dmag.ToLoad.Measure it) {
		var measure = new Measure();
		measure.setAssayCode(getOntologyTerm(it.getAssayCode()));
		measure.setDate(Date.valueOf(it.getDate()));
		measure.setMeasurementValue(getMeasurementValue(it.getMeasurementValue()));
		measureRepository.save(measure);
		return measure;
	}

	private MeasurementValue getMeasurementValue(edu.upc.dmag.ToLoad.MeasurementValue readMeasurementValue) {
		var measurementValue = new MeasurementValue();
		if (readMeasurementValue.getQuantity() != null){
			var value = new Value();
			if(readMeasurementValue.getQuantity().getOntologyTerm() != null){
				value.setTermValue(getOntologyTerm(readMeasurementValue.getQuantity().getOntologyTerm()));
			}else {
				var quantity = getQuantity(readMeasurementValue.getQuantity());
				value.setQuantity(quantity);
			}
			valueRepository.save(value);
			measurementValue.setValue(value);
		} else {
			var complexValue = new ComplexValue();
			complexValue.setQuantity(getQuantity(readMeasurementValue.getTypedQuantities().getQuantity()));
			complexValue.setQuantityType(getQuantityType(readMeasurementValue.getTypedQuantities().getQuantityType()));
			complexValueRepository.save(complexValue);
			measurementValue.setComplexValue(complexValue);
		}
		measurementValueRepository.save(measurementValue);
		return measurementValue;
	}

	private OntologyTerm getQuantityType(QuantityType readType) {
		var foundTerm = ontologyTermRepository.findById(readType.getId());
		if (foundTerm.isPresent()) {
			return foundTerm.get();
		} else {
			OntologyTerm ontologyTerm = new OntologyTerm();
			ontologyTerm.setId(readType.getId());
			ontologyTerm.setLabel(readType.getLabel());
			ontologyTermRepository.save(ontologyTerm);
			return ontologyTerm;
		}
	}

	private Quantity getQuantity(Quantity__1 readQuantity) {
		var quantity = new Quantity();
		quantity.setValue(readQuantity.getValue());
		quantity.setUnit(getOntologyTerm(readQuantity.getUnit()));
		if (quantity.getReferenceRange() != null){
			var referenceRange = new ReferenceRange();
			referenceRange.setLow(readQuantity.getReferenceRange().getLow());
			referenceRange.setHigh(readQuantity.getReferenceRange().getHigh());
			referenceRange.setUnit(getOntologyTerm(readQuantity.getReferenceRange().getUnit()));
			quantity.setReferenceRange(referenceRange);
		}
		quantityRepository.save(quantity);
		return quantity;
	}

	private OntologyTerm getOntologyTerm(Unit__4 unit) {
		var foundTerm = ontologyTermRepository.findById(unit.getId());
		if (foundTerm.isPresent()) {
			return foundTerm.get();
		} else {
			OntologyTerm ontologyTerm = new OntologyTerm();
			ontologyTerm.setId(unit.getId());
			ontologyTerm.setLabel(unit.getLabel());
			ontologyTermRepository.save(ontologyTerm);
			return ontologyTerm;
		}
	}

	private OntologyTerm getOntologyTerm(Unit__3 unit) {
		var foundTerm = ontologyTermRepository.findById(unit.getId());
		if (foundTerm.isPresent()) {
			return foundTerm.get();
		} else {
			OntologyTerm ontologyTerm = new OntologyTerm();
			ontologyTerm.setId(unit.getId());
			ontologyTerm.setLabel(unit.getLabel());
			ontologyTermRepository.save(ontologyTerm);
			return ontologyTerm;
		}
	}

	@NotNull
	private Quantity getQuantity(edu.upc.dmag.ToLoad.Quantity readQuantity) {
		var quantity = new Quantity();
		quantity.setValue(readQuantity.getValue());
		quantity.setUnit(getOntologyTerm(readQuantity.getUnit()));
		if (quantity.getReferenceRange() != null){
			var referenceRange = new ReferenceRange();
			referenceRange.setLow(readQuantity.getReferenceRange().getLow());
			referenceRange.setHigh(readQuantity.getReferenceRange().getHigh());
			referenceRange.setUnit(getOntologyTerm(readQuantity.getReferenceRange().getUnit()));
			quantity.setReferenceRange(referenceRange);
		}
		quantityRepository.save(quantity);
		return quantity;
	}

	private OntologyTerm getOntologyTerm(Unit__2 unit) {
		var foundTerm = ontologyTermRepository.findById(unit.getId());
		if (foundTerm.isPresent()) {
			return foundTerm.get();
		} else {
			OntologyTerm ontologyTerm = new OntologyTerm();
			ontologyTerm.setId(unit.getId());
			ontologyTerm.setLabel(unit.getLabel());
			ontologyTermRepository.save(ontologyTerm);
			return ontologyTerm;
		}
	}

	private OntologyTerm getOntologyTerm(Unit__1 unit) {
		var foundTerm = ontologyTermRepository.findById(unit.getId());
		if (foundTerm.isPresent()) {
			return foundTerm.get();
		} else {
			OntologyTerm ontologyTerm = new OntologyTerm();
			ontologyTerm.setId(unit.getId());
			ontologyTerm.setLabel(unit.getLabel());
			ontologyTermRepository.save(ontologyTerm);
			return ontologyTerm;
		}
	}

	private OntologyTerm getOntologyTerm(edu.upc.dmag.ToLoad.OntologyTerm readOntologyTerm) {
		var foundTerm = ontologyTermRepository.findById(readOntologyTerm.getId());
		if (foundTerm.isPresent()) {
			return foundTerm.get();
		} else {
			OntologyTerm ontologyTerm = new OntologyTerm();
			ontologyTerm.setId(readOntologyTerm.getId());
			ontologyTerm.setLabel(readOntologyTerm.getLabel());
			ontologyTermRepository.save(ontologyTerm);
			return ontologyTerm;
		}
	}

	private OntologyTerm getOntologyTerm(AssayCode__1 assayCode) {
		var foundTerm = ontologyTermRepository.findById(assayCode.getId());
		if (foundTerm.isPresent()) {
			return foundTerm.get();
		} else {
			OntologyTerm ontologyTerm = new OntologyTerm();
			ontologyTerm.setId(assayCode.getId());
			ontologyTerm.setLabel(assayCode.getLabel());
			ontologyTermRepository.save(ontologyTerm);
			return ontologyTerm;
		}
	}

	private Set<PhenotypicFeature> getPhenotypicFeatures(List<PhenotypicFeature__1> phenotypicFeatures) {
		var result = new HashSet<PhenotypicFeature>();

		for (var phenotypicFeature : phenotypicFeatures){
			result.add(getPhenotypicFeature(phenotypicFeature));
		}

		return result;

	}

	private PhenotypicFeature getPhenotypicFeature(PhenotypicFeature__1 readPhenotypicFeature) {
		var phenotypicFeature = new PhenotypicFeature();
		phenotypicFeature.setFeatureType(getOntologyTerm(readPhenotypicFeature.getFeatureType()));
		phenotypicFeature.setModifiers(getOntologyTermsModifiers(readPhenotypicFeature.getModifiers()));
		phenotypicFeatureRepository.save(phenotypicFeature);
		return phenotypicFeature;
	}

	private Set<OntologyTerm> getOntologyTermsModifiers(List<Modifier__3> modifiers) {
		return modifiers.stream().map(this::getOntologyTerm).collect(Collectors.toSet());
	}

	private OntologyTerm getOntologyTerm(Modifier__3 modifier__3) {
		var foundTerm = ontologyTermRepository.findById(modifier__3.getId());
		if (foundTerm.isPresent()) {
			return foundTerm.get();
		} else {
			OntologyTerm ontologyTerm = new OntologyTerm();
			ontologyTerm.setId(modifier__3.getId());
			ontologyTerm.setLabel(modifier__3.getLabel());
			ontologyTermRepository.save(ontologyTerm);
			return ontologyTerm;
		}
	}

	private OntologyTerm getOntologyTerm(FeatureType__3 featureType) {
		var foundTerm = ontologyTermRepository.findById(featureType.getId());
		if (foundTerm.isPresent()) {
			return foundTerm.get();
		} else {
			OntologyTerm ontologyTerm = new OntologyTerm();
			ontologyTerm.setId(featureType.getId());
			ontologyTerm.setLabel(featureType.getLabel());
			ontologyTermRepository.save(ontologyTerm);
			return ontologyTerm;
		}
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

	private void loadReadBiosample(BiosamplesSchema readBiosample) {
		var biosample = new Biosample();
		biosample.setId(readBiosample.getId());
		biosample.setObtentionProcedure(getObtentionProcedure(readBiosample.getObtentionProcedure()));
		biosample.setBiosampleStatus(getOntologyTerm(readBiosample.getBiosampleStatus()));
		biosample.setSampleOriginType(getOntologyTerm(readBiosample.getSampleOriginType()));
		biosample.setIndividual(individualRepository.getReferenceById(readBiosample.getIndividualId()));
		biosampleRepository.save(biosample);
	}

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
		} else if (readObtentionProcedure.getAgeAtProcedure().getAgeRange() != null){
			obtentionProcedure.setAge(getAgeRange(readObtentionProcedure.getAgeAtProcedure().getAgeRange()));
		} else if (readObtentionProcedure.getAgeAtProcedure().getGestationalAge() != null){
			obtentionProcedure.setAge(
					getGestionalAge(readObtentionProcedure.getAgeAtProcedure().getGestationalAge())
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

	private Age getGestionalAge(Integer readGestationalAge) {
		var gestionalAge = new GestationalAge(readGestationalAge);
		ageRepository.save(gestionalAge);
		return gestionalAge;
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
		var ageDuration = new AgeDuration(
			duration
		);
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
}
package edu.upc.dmag.beaconLoaderWithCLI;


import edu.upc.dmag.beaconLoaderWithCLI.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;

@SpringBootApplication
public class BeaconLoaderWithCliApplication implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory
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
	private final JobLauncher jobLauncher;
	private final Job loadDatasetsJob;
	private final Job loadIndividualsJob;
	private final Job loadBiosamplesJob;
	private final Job loadRunsJob;
	private final Job loadAnalysesJob;
	private final Job loadCohortsJob;
	private final VariationHaplotypeMemberRepository variationHaplotypeMemberRepository;
	private final JdbcTemplate jdbcTemplate;
	private final Job loadGenomicVariantsJob;

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
            MolecularAttributeRepository molecularAttributeRepository,
            VariationHaplotypeMemberRepository variationHaplotypeMemberRepository,
            JobLauncher jobLauncher,
            Job loadDatasetsJob,
            Job loadIndividualsJob,
            Job loadBiosamplesJob,
            Job loadRunsJob,
            Job loadAnalysesJob,
            Job loadCohortsJob,
            JdbcTemplate jdbcTemplate, @Qualifier("loadGenomicVariantsJob") Job loadGenomicVariantsJob) {
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
		this.variationHaplotypeMemberRepository = variationHaplotypeMemberRepository;
		this.jobLauncher = jobLauncher;
		this.loadDatasetsJob = loadDatasetsJob;
		this.loadIndividualsJob = loadIndividualsJob;
		this.loadBiosamplesJob = loadBiosamplesJob;
		this.loadRunsJob = loadRunsJob;
		this.loadAnalysesJob = loadAnalysesJob;
		this.loadCohortsJob = loadCohortsJob;
		this.jdbcTemplate = jdbcTemplate;
		this.loadGenomicVariantsJob = loadGenomicVariantsJob;
	}

	public static void main(String[] args) {
		LOG.info("STARTING THE APPLICATION");
		SpringApplication.run(BeaconLoaderWithCliApplication.class, args);
		LOG.info("APPLICATION FINISHED");
	}

	@Override
	public void run(String... args) throws Exception {
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

	public void deleteAll() {
		System.out.println("deleting all");

		// Usar TRUNCATE CASCADE de PostgreSQL para eliminar todas las tablas
		// sin preocuparse por el orden de las foreign keys
		String[] tables = {
			"dataset", "molecular_attribute", "frequency_in_population", "frequency_in_populations",
			"variant_alternative_id", "genomic_variation", "variation", "variation_haplotype_member",
			"case_level_data", "analysis", "run", "biosample", "ontology_term", "obtention_procedure",
			"measure", "individual", "library_selection", "age_range_criteria", "cohort",
			"phenotypic_feature", "data_use_conditions", "age", "measurement_value", "complex_value",
			"reference_range", "quantity", "value", "phenotypic_effect", "clinical_interpretation",
			"interval", "variant_level_data", "location"
		};

		for (String table : tables) {
			try {
				jdbcTemplate.execute("TRUNCATE TABLE " + table + " CASCADE");
			} catch (Exception e) {
				// La tabla podría no existir, ignorar
				LOG.warn("Could not truncate table: " + table + " - " + e.getMessage());
			}
		}

		System.out.println("deleted all");
	}

	private void loadGenomicVariations() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("startAt", System.currentTimeMillis())
				.toJobParameters();
		jobLauncher.run(loadGenomicVariantsJob, jobParameters);
	}




	private void loadCohorts() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("startAt", System.currentTimeMillis())
				.toJobParameters();
		jobLauncher.run(loadCohortsJob, jobParameters);
    }

	private void loadDatasets() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("startAt", System.currentTimeMillis())
				.toJobParameters();
		jobLauncher.run(loadDatasetsJob, jobParameters);
    }

	private void loadRuns() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("startAt", System.currentTimeMillis())
				.toJobParameters();
		jobLauncher.run(loadRunsJob, jobParameters);
    }

	private void loadAnalyses() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("startAt", System.currentTimeMillis())
				.toJobParameters();
		jobLauncher.run(loadAnalysesJob, jobParameters);
	}

	private void loadBiosamples() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("startAt", System.currentTimeMillis())
				.toJobParameters();
		jobLauncher.run(loadBiosamplesJob, jobParameters);
    }

	private void loadIndividuals() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("startAt", System.currentTimeMillis())
				.toJobParameters();
		jobLauncher.run(loadIndividualsJob, jobParameters);
	}
}
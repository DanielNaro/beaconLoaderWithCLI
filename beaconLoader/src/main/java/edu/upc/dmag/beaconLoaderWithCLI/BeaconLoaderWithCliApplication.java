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
	private final JobLauncher jobLauncher;
	private final Job loadDatasetsJob;
	private final Job loadIndividualsJob;
	private final Job loadBiosamplesJob;
	private final Job loadRunsJob;
	private final Job loadAnalysesJob;
	private final Job loadCohortsJob;
	private final JdbcTemplate jdbcTemplate;
	private final Job loadGenomicVariantsJob;

	public BeaconLoaderWithCliApplication(

            JobLauncher jobLauncher,
            Job loadDatasetsJob,
            Job loadIndividualsJob,
            Job loadBiosamplesJob,
            Job loadRunsJob,
            Job loadAnalysesJob,
            Job loadCohortsJob,
            JdbcTemplate jdbcTemplate, @Qualifier("loadGenomicVariantsJob") Job loadGenomicVariantsJob) {
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
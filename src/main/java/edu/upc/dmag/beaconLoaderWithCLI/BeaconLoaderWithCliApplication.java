package edu.upc.dmag.beaconLoaderWithCLI;


import com.google.gson.Gson;
import edu.upc.dmag.ToLoad.*;
import edu.upc.dmag.ToLoad.AgeRange;
import edu.upc.dmag.ToLoad.DuoDataUse;
import edu.upc.dmag.ToLoad.ObtentionProcedure;
import edu.upc.dmag.beaconLoaderWithCLI.entities.*;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Age;
import edu.upc.dmag.beaconLoaderWithCLI.entities.DataUseConditions;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Measure;
import edu.upc.dmag.beaconLoaderWithCLI.entities.PhenotypicFeature;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
			AgeRepository ageRepository
	) {
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
	}

	public static void main(String[] args) {
		LOG.info("STARTING THE APPLICATION");
		SpringApplication.run(BeaconLoaderWithCliApplication.class, args);
		LOG.info("APPLICATION FINISHED");
	}

	@Override
	public void run(String... args) throws IOException {
		LOG.info("EXECUTING : command line runner");

		//deleteAll();
		//loadData();
		loadDatasets();
		loadIndividuals();
		loadBiosamples();
		loadRuns();
		loadAnalyses();
		loadCohorts();
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
		measureRepository.save(measure);
		return measure;
	}

	private OntologyTerm getOntologyTerm(AssayCode__1 assayCode) {
		return null;
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
		phenotypicFeature.setModifiers(getOntologyTerms(readPhenotypicFeature.getModifiers()));
		phenotypicFeatureRepository.save(phenotypicFeature);
		return phenotypicFeature;
	}

	private Set<OntologyTerm> getOntologyTerms(List<Modifier__5> modifiers) {
		return modifiers.stream().map(this::getOntologyTerm).collect(Collectors.toSet());

	}

	private OntologyTerm getOntologyTerm(Modifier__5 readModifier) {
		var foundTerm = ontologyTermRepository.findById(readModifier.getId());
		if (foundTerm.isPresent()) {
			return foundTerm.get();
		} else {
			OntologyTerm ontologyTerm = new OntologyTerm();
			ontologyTerm.setId(readModifier.getId());
			ontologyTerm.setLabel(readModifier.getLabel());
			ontologyTermRepository.save(ontologyTerm);
			return ontologyTerm;
		}
	}

	private OntologyTerm getOntologyTerm(FeatureType__5 featureType) {
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
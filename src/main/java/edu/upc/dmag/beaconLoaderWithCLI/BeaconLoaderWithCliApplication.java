package edu.upc.dmag.beaconLoaderWithCLI;


import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.*;
import org.openapitools.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootApplication
public class BeaconLoaderWithCliApplication implements CommandLineRunner {

	private static Logger LOG = LoggerFactory
			.getLogger(BeaconLoaderWithCliApplication.class);

	public static void main(String[] args) {
		LOG.info("STARTING THE APPLICATION");
		SpringApplication.run(BeaconLoaderWithCliApplication.class, args);
		LOG.info("APPLICATION FINISHED");
	}

	@Override
	public void run(String... args) throws ApiException, IOException {
		LOG.info("EXECUTING : command line runner");

		deleteAll();
		loadData();
	}

	private void loadData() throws IOException, ApiException {
		Map<String, Individual> createdIndividuals = new HashMap<>();
		Map<String, Biosample> createdBiosamples = new HashMap<>();
		Map<ReadBiosampleStatus, UUID> createdBioSampleStatuses = new HashMap<>();
		Map<ReadObtentionProcedure, UUID> createdBiosampleObtenitionProcedures = new HashMap<>();
		Map<ReadBiosampleOriginType, BiosampleSampleOrigin> createdBiosampleOrigins = new HashMap<>();
		loadDatasets();
		loadIndividuals(createdIndividuals);
		loadBiosamples(createdIndividuals, createdBiosamples, createdBioSampleStatuses, createdBiosampleObtenitionProcedures, createdBiosampleOrigins);
	}

	private void loadBiosamples(
			Map<String, Individual> createdIndividuals,
			Map<String, Biosample> createdBiosamples,
			Map<ReadBiosampleStatus, UUID> createdBioSampleStatuses,
			Map<ReadObtentionProcedure, UUID> createdBiosampleObtenitionProcedures,
			Map<ReadBiosampleOriginType, BiosampleSampleOrigin> createdBiosampleOrigins
	) throws IOException, ApiException {
		try (InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream("./src/main/resources/toLoad/biosamples.json"))){
			Gson gson = new Gson();
			var readBiosamples = gson.fromJson(jsonFileInputStream, ReadBiosample[].class);

			BiosampleResourceApi biosampleResourceApi = new BiosampleResourceApi();
			biosampleResourceApi.setApiClient(getApiClient());
			BiosampleStatusResourceApi biosampleStatusResourceApi = new BiosampleStatusResourceApi();
			biosampleStatusResourceApi.setApiClient(getApiClient());
			BiosampleObtentionProcedureResourceApi biosampleObtentionProcedureResourceApi = new BiosampleObtentionProcedureResourceApi();
			biosampleObtentionProcedureResourceApi.setApiClient(getApiClient());

			for(ReadBiosample readBiosample: readBiosamples){
				loadReadBiosample(
						biosampleResourceApi,
						readBiosample,
						createdBioSampleStatuses,
						createdBiosampleObtenitionProcedures,
						createdBiosampleOrigins,
						biosampleStatusResourceApi,
						biosampleObtentionProcedureResourceApi
				);
			}
		}
	}

	private void loadReadBiosample(
			BiosampleResourceApi biosampleResourceApi,
			ReadBiosample readBiosample,
			Map<ReadBiosampleStatus, UUID> createdBioSampleStatuses,
			Map<ReadObtentionProcedure, UUID> createdBiosampleObtenitionProcedures,
			Map<ReadBiosampleOriginType, BiosampleSampleOrigin> createdBiosampleOrigins,
			BiosampleStatusResourceApi biosampleStatusResourceApi,
			BiosampleObtentionProcedureResourceApi BiosampleObtentionProcedureResourceApi
	) throws ApiException {
		var createdBioSample = biosampleResourceApi.createBiosample(readBiosample.getAPIRepresentation(
				createdBioSampleStatuses,
				createdBiosampleObtenitionProcedures,
				createdBiosampleOrigins,
				biosampleStatusResourceApi,
				BiosampleObtentionProcedureResourceApi
		));

		if (readBiosample.getBiosampleOriginType() != null) {
			if (!createdBiosampleOrigins.containsKey(readBiosample.getBiosampleOriginType())){
				createdBiosampleOrigins.put(readBiosample.getBiosampleOriginType(), createdBioSample.getSampleOrigin());
			}
		}
	}

	private void loadIndividuals(Map<String, Individual> createdIndividuals) throws IOException, ApiException {
		try (InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream("./src/main/resources/toLoad/individuals.json"))){
			Gson gson = new Gson();
			var readIndividuals = gson.fromJson(jsonFileInputStream, ReadIndividual[].class);

			IndividualResourceApi individualResourceApi = new IndividualResourceApi();
			individualResourceApi.setApiClient(getApiClient());
			MeasureResourceApi measureResourceApi = new MeasureResourceApi();
			measureResourceApi.setApiClient(getApiClient());

			for(ReadIndividual readIndividual: readIndividuals){
				loadReadIndividual(individualResourceApi, measureResourceApi, readIndividual, createdIndividuals);
			}
		}
	}

	private void loadReadIndividual(IndividualResourceApi individualResourceApi, MeasureResourceApi measureResourceApi, ReadIndividual readIndividual, Map<String, Individual> createdIndividuals) throws ApiException {
		Individual createdIndividual = individualResourceApi.createIndividual(
				readIndividual.getAPIRepresentation()
		);
		createdIndividuals.put(readIndividual.getId(), createdIndividual);
	}

	private void loadDatasets() throws IOException, ApiException {
		try (InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream("./src/main/resources/toLoad/datasets.json"))){
			Gson gson = new Gson();
			var readDatasets = gson.fromJson(jsonFileInputStream, ReadDataset[].class);

			DatasetResourceApi datasetResourceApi = new DatasetResourceApi();
			datasetResourceApi.setApiClient(getApiClient());

			for(ReadDataset readDataset: readDatasets){
				loadReadDataset(datasetResourceApi, readDataset);
			}
		}
	}

	private void loadReadDataset(DatasetResourceApi datasetResourceApi, ReadDataset readDataset) throws ApiException {
		datasetResourceApi.createDataset(
				readDataset.getAPIRepresentation()
		);
	}


	private static void deleteAll() throws ApiException {
		deleteDatasets();
		/*deleteIndividuals();
		deleteRuns();
		deleteBioSamples();
		deleteCohort();
		deleteCaseLevelData();
		deleteZigosity();
		deleteGenomicVariation();
		deleteVariations();
		deleteVariationLocations();
		deleteVariantPositions();
		deleteVariantIdentifier();
		deleteTumorGrade();
		deleteTumorProgression();
		deleteVariantInfo();
		deleteSampleProcessing();
		deleteSampleStorage();
		deleteSeverityLevel();
		deleteTreatmentRoute();
		deleteTreatment();
		deleteFamilyHistory();
		deleteBiosampleStatus();
		deleteBiosampleObtentionProcedure();
		deleteBodySite();
		deleteCohortDesign();
		deleteDiagnosticMarker();
		deleteDiseaseStage();
		deleteDiseases();
		deleteEthnicity();
		deleteHistologicalDiagnosis();
		deleteIndividualTreatment();
		deleteLibrarySource();
		deleteGene();
		deleteMeasure();
		deleteAminoacidChange();
		deleteAnnotationImpact();
		deleteMolecularAttribute();
		deleteMolecularEffect();
		deletePhenotypicFeatureEvidence();
		deletePipeline();
		deleteProcedureOnIndividual();
		deleteProcedureForMeasurement();
		deleteAnalysis();
		deletePlatformModel();
		deleteProcedure();
		deleteSampleOrigin();*/
	}

	@NotNull
	private static ApiClient getApiClient() {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("username", "admin");
		parameters.put("password","admin");

        return new ApiClient(
				"http://localhost:9080/realms/jhipster/protocol/openid-connect/token",
				"web_app",
				"web_app",
				parameters
		);
	}

	private static void deleteDatasets() throws ApiException {
		DatasetResourceApi datasetResourceApi = new DatasetResourceApi();
		datasetResourceApi.setApiClient(getApiClient());

		var datasets = datasetResourceApi.getAllDatasets(Boolean.TRUE);
		var datasetUUIDs = datasets.stream().map(Dataset::getId).collect(Collectors.toSet());

		for (var datasetUUID: datasetUUIDs){
			datasetResourceApi.deleteDataset(datasetUUID);
		}
	}

	private static void deleteIndividuals() throws ApiException {
		IndividualResourceApi individualResourceApi = new IndividualResourceApi();
		individualResourceApi.setApiClient(getApiClient());

		var individuals = individualResourceApi.getAllIndividuals(Boolean.FALSE);
		var individualsUUIDs = individuals.stream().map(Individual::getId).collect(Collectors.toSet());

		for (var individualUUID: individualsUUIDs){
			individualResourceApi.deleteIndividual(individualUUID);
		}
	}

	private static void deleteRuns() throws ApiException {
		RunResourceApi runResourceApi = new RunResourceApi();
		runResourceApi.setApiClient(getApiClient());

		var runs = runResourceApi.getAllRuns();
		var runsUUIDs = runs.stream().map(Run::getId).collect(Collectors.toSet());

		for (var runUUID: runsUUIDs){
			runResourceApi.deleteRun(runUUID);
		}
	}

	private static void deleteBioSamples() throws ApiException {
		BiosampleResourceApi biosampleResourceApi = new BiosampleResourceApi();
		biosampleResourceApi.setApiClient(getApiClient());

		var runs = biosampleResourceApi.getAllBiosamples(Boolean.TRUE);
		var runsUUIDs = runs.stream().map(Biosample::getId).collect(Collectors.toSet());

		for (var runUUID: runsUUIDs){
			biosampleResourceApi.deleteBiosample(runUUID);
		}
	}

	private static void deleteCohort() throws ApiException {
		CohortResourceApi cohortResourceApi = new CohortResourceApi();
		cohortResourceApi.setApiClient(getApiClient());

		var cohorts = cohortResourceApi.getAllCohorts(Boolean.TRUE);
		var cohortUUIDs = cohorts.stream().map(Cohort::getId).collect(Collectors.toSet());

		for (var cohortUUID: cohortUUIDs){
			cohortResourceApi.deleteCohort(cohortUUID);
		}
	}

	private static void deleteCaseLevelData() throws ApiException {
		CaseLevelDataResourceApi caseLevelDataResourceApi = new CaseLevelDataResourceApi();
		caseLevelDataResourceApi.setApiClient(getApiClient());

		var cohorts = caseLevelDataResourceApi.getAllCaseLevelData();
		var cohortUUIDs = cohorts.stream().map(CaseLevelData::getId).collect(Collectors.toSet());

		for (var cohortUUID: cohortUUIDs){
			caseLevelDataResourceApi.deleteCaseLevelData(cohortUUID);
		}
	}

	private static void deleteZigosity() throws ApiException {
		ZigosityResourceApi zigosityResourceApi = new ZigosityResourceApi();
		zigosityResourceApi.setApiClient(getApiClient());

		var cohorts = zigosityResourceApi.getAllZigosities();
		var cohortUUIDs = cohorts.stream().map(Zigosity::getId).collect(Collectors.toSet());

		for (var cohortUUID: cohortUUIDs){
			zigosityResourceApi.deleteZigosity(cohortUUID);
		}
	}

	private static void deleteGenomicVariation() throws ApiException {
		GenomicVariationResourceApi genomicVariationResourceApi = new GenomicVariationResourceApi();
		genomicVariationResourceApi.setApiClient(getApiClient());

		var cohorts = genomicVariationResourceApi.getAllGenomicVariations(Boolean.FALSE);
		var cohortUUIDs = cohorts.stream().map(GenomicVariation::getId).collect(Collectors.toSet());

		for (var cohortUUID: cohortUUIDs){
			genomicVariationResourceApi.deleteGenomicVariation(cohortUUID);
		}
	}

	private static void deleteVariations() throws ApiException {
		VariationResourceApi variationResourceApi = new VariationResourceApi();
		variationResourceApi.setApiClient(getApiClient());

		var variations = variationResourceApi.getAllVariations();
		var variationUUIDs = variations.stream().map(Variation::getId).collect(Collectors.toSet());

		for (var variationUUID: variationUUIDs){
			variationResourceApi.deleteVariation(variationUUID);
		}
	}
	private static void deleteVariationLocations() throws ApiException {
		VariationLocationResourceApi variationLocationResourceApi = new VariationLocationResourceApi();
		variationLocationResourceApi.setApiClient(getApiClient());

		var instances = variationLocationResourceApi.getAllVariationLocations();
		var instancesUUIDs = instances.stream().map(VariationLocation::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			variationLocationResourceApi.deleteVariationLocation(instanceUUID);
		}
	}

	private static void deleteVariantPositions() throws ApiException {
		VariantPositionResourceApi variantPositionResourceApi = new VariantPositionResourceApi();
		variantPositionResourceApi.setApiClient(getApiClient());

		var instances = variantPositionResourceApi.getAllVariantPositions();
		var instancesUUIDs = instances.stream().map(VariantPosition::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			variantPositionResourceApi.deleteVariantPosition(instanceUUID);
		}
	}

	private static void deleteVariantInfo() throws ApiException {
		VariantInfoResourceApi variantInfoResourceApi = new VariantInfoResourceApi();
		variantInfoResourceApi.setApiClient(getApiClient());

		var instances = variantInfoResourceApi.getAllVariantInfos();
		var instancesUUIDs = instances.stream().map(VariantInfo::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			variantInfoResourceApi.deleteVariantInfo(instanceUUID);
		}
	}

	private static void deleteVariantIdentifier() throws ApiException {
		VariantIdentifierResourceApi variantIdentifierResourceApi = new VariantIdentifierResourceApi();
		variantIdentifierResourceApi.setApiClient(getApiClient());

		var instances = variantIdentifierResourceApi.getAllVariantIdentifiers();
		var instancesUUIDs = instances.stream().map(VariantIdentifier::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			variantIdentifierResourceApi.deleteVariantIdentifier(instanceUUID);
		}
	}

	private static void deleteTumorProgression() throws ApiException {
		TumorProgressionResourceApi tumorProgressionResourceApi = new TumorProgressionResourceApi();
		tumorProgressionResourceApi.setApiClient(getApiClient());

		var instances = tumorProgressionResourceApi.getAllTumorProgressions();
		var instancesUUIDs = instances.stream().map(TumorProgression::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			tumorProgressionResourceApi.deleteTumorProgression(instanceUUID);
		}
	}

	private static void deleteTumorGrade() throws ApiException {
		TumorGradeResourceApi tumorGradeResourceApi = new TumorGradeResourceApi();
		tumorGradeResourceApi.setApiClient(getApiClient());

		var instances = tumorGradeResourceApi.getAllTumorGrades();
		var instancesUUIDs = instances.stream().map(TumorGrade::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			tumorGradeResourceApi.deleteTumorGrade(instanceUUID);
		}
	}

	private static void deleteTreatment() throws ApiException {
		TreatmentResourceApi treatmentResourceApi = new TreatmentResourceApi();
		treatmentResourceApi.setApiClient(getApiClient());

		var instances = treatmentResourceApi.getAllTreatments();
		var instancesUUIDs = instances.stream().map(Treatment::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			treatmentResourceApi.deleteTreatment(instanceUUID);
		}
	}

	private static void deleteTreatmentRoute() throws ApiException {
		TreatmentRouteResourceApi treatmentRouteResourceApi = new TreatmentRouteResourceApi();
		treatmentRouteResourceApi.setApiClient(getApiClient());

		var instances = treatmentRouteResourceApi.getAllTreatmentRoutes();
		var instancesUUIDs = instances.stream().map(TreatmentRoute::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			treatmentRouteResourceApi.deleteTreatmentRoute(instanceUUID);
		}
	}

	private static void deleteSeverityLevel() throws ApiException {
		SeverityLevelResourceApi severityLevelResourceApi = new SeverityLevelResourceApi();
		severityLevelResourceApi.setApiClient(getApiClient());

		var instances = severityLevelResourceApi.getAllSeverityLevels();
		var instancesUUIDs = instances.stream().map(SeverityLevel::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			severityLevelResourceApi.deleteSeverityLevel(instanceUUID);
		}
	}

	private static void deleteSampleStorage() throws ApiException {
		SampleStorageResourceApi sampleStorageResourceApi = new SampleStorageResourceApi();
		sampleStorageResourceApi.setApiClient(getApiClient());

		var instances = sampleStorageResourceApi.getAllSampleStorages();
		var instancesUUIDs = instances.stream().map(SampleStorage::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			sampleStorageResourceApi.deleteSampleStorage(instanceUUID);
		}
	}

	private static void deleteSampleProcessing() throws ApiException {
		SampleProcessingResourceApi sampleProcessingResourceApi = new SampleProcessingResourceApi();
		sampleProcessingResourceApi.setApiClient(getApiClient());

		var instances = sampleProcessingResourceApi.getAllSampleProcessings();
		var instancesUUIDs = instances.stream().map(SampleProcessing::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			sampleProcessingResourceApi.deleteSampleProcessing(instanceUUID);
		}
	}

	private static void deleteSampleOrigin() throws ApiException {
		SampleOriginResourceApi sampleOriginResourceApi = new SampleOriginResourceApi();
		sampleOriginResourceApi.setApiClient(getApiClient());

		var instances = sampleOriginResourceApi.getAllSampleOrigins();
		var instancesUUIDs = instances.stream().map(SampleOrigin::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			sampleOriginResourceApi.deleteSampleOrigin(instanceUUID);
		}
	}

	private static void deleteAnalysis() throws ApiException {
		AnalysisResourceApi analysisResourceApi = new AnalysisResourceApi();
		analysisResourceApi.setApiClient(getApiClient());

		var instances = analysisResourceApi.getAllAnalyses();
		var instancesUUIDs = instances.stream().map(Analysis::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			analysisResourceApi.deleteAnalysis(instanceUUID);
		}
	}

	private static void deletePlatformModel() throws ApiException {
		PlatformModelResourceApi platformModelResourceApi = new PlatformModelResourceApi();
		platformModelResourceApi.setApiClient(getApiClient());

		var instances = platformModelResourceApi.getAllPlatformModels();
		var instancesUUIDs = instances.stream().map(PlatformModel::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			platformModelResourceApi.deletePlatformModel(instanceUUID);
		}
	}

	private static void deleteProcedure() throws ApiException {
		ProcedureResourceApi platformModelResourceApi = new ProcedureResourceApi();
		platformModelResourceApi.setApiClient(getApiClient());

		var instances = platformModelResourceApi.getAllProcedures();
		var instancesUUIDs = instances.stream().map(Procedure::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			platformModelResourceApi.deleteProcedure(instanceUUID);
		}
	}

	private static void deleteProcedureOnIndividual() throws ApiException {
		ProcedureOnIndividualResourceApi procedureOnIndividualResourceApi = new ProcedureOnIndividualResourceApi();
		procedureOnIndividualResourceApi.setApiClient(getApiClient());

		var instances = procedureOnIndividualResourceApi.getAllProcedureOnIndividuals();
		var instancesUUIDs = instances.stream().map(ProcedureOnIndividual::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			procedureOnIndividualResourceApi.deleteProcedureOnIndividual(instanceUUID);
		}
	}

	private static void deleteProcedureForMeasurement() throws ApiException {
		ProcedureForMeasurementResourceApi procedureForMeasurementResourceApi = new ProcedureForMeasurementResourceApi();
		procedureForMeasurementResourceApi.setApiClient(getApiClient());

		var instances = procedureForMeasurementResourceApi.getAllProcedureForMeasurements();
		var instancesUUIDs = instances.stream().map(ProcedureForMeasurement::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			procedureForMeasurementResourceApi.deleteProcedureForMeasurement(instanceUUID);
		}
	}

	private static void deletePipeline() throws ApiException {
		PipelineResourceApi pipelineResourceApi = new PipelineResourceApi();
		pipelineResourceApi.setApiClient(getApiClient());

		var instances = pipelineResourceApi.getAllPipelines();
		var instancesUUIDs = instances.stream().map(Pipeline::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			pipelineResourceApi.deletePipeline(instanceUUID);
		}
	}

	private static void deletePhenotypicFeatureEvidence() throws ApiException {
		PhenotypicFeatureEvidenceResourceApi phenotypicFeatureEvidenceResourceApi = new PhenotypicFeatureEvidenceResourceApi();
		phenotypicFeatureEvidenceResourceApi.setApiClient(getApiClient());

		var instances = phenotypicFeatureEvidenceResourceApi.getAllPhenotypicFeatureEvidences();
		var instancesUUIDs = instances.stream().map(PhenotypicFeatureEvidence::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			phenotypicFeatureEvidenceResourceApi.deletePhenotypicFeatureEvidence(instanceUUID);
		}
	}


	private static void deleteMolecularAttribute() throws ApiException {
		MolecularAttributeResourceApi molecularAttributeResourceApi = new MolecularAttributeResourceApi();
		molecularAttributeResourceApi.setApiClient(getApiClient());

		var instances = molecularAttributeResourceApi.getAllMolecularAttributes(Boolean.FALSE);
		var instancesUUIDs = instances.stream().map(MolecularAttribute::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			molecularAttributeResourceApi.deleteMolecularAttribute(instanceUUID);
		}
	}

	private static void deleteMolecularEffect() throws ApiException {
		MolecularEffectResourceApi molecularEffectResourceApi = new MolecularEffectResourceApi();
		molecularEffectResourceApi.setApiClient(getApiClient());

		var instances = molecularEffectResourceApi.getAllMolecularEffects();
		var instancesUUIDs = instances.stream().map(MolecularEffect::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			molecularEffectResourceApi.deleteMolecularEffect(instanceUUID);
		}
	}

	private static void deleteAminoacidChange() throws ApiException {
		AminoacidChangeResourceApi aminoacidChangeResourceApi = new AminoacidChangeResourceApi();
		aminoacidChangeResourceApi.setApiClient(getApiClient());

		var instances = aminoacidChangeResourceApi.getAllAminoacidChanges();
		var instancesUUIDs = instances.stream().map(AminoacidChange::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			aminoacidChangeResourceApi.deleteAminoacidChange(instanceUUID);
		}
	}

	private static void deleteAnnotationImpact() throws ApiException {
		AnnotationImpactResourceApi annotationImpactResourceApi = new AnnotationImpactResourceApi();
		annotationImpactResourceApi.setApiClient(getApiClient());

		var instances = annotationImpactResourceApi.getAllAnnotationImpacts();
		var instancesUUIDs = instances.stream().map(AnnotationImpact::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			annotationImpactResourceApi.deleteAnnotationImpact(instanceUUID);
		}
	}

	private static void deleteGene() throws ApiException {
		GeneResourceApi geneResourceApi = new GeneResourceApi();
		geneResourceApi.setApiClient(getApiClient());

		var instances = geneResourceApi.getAllGenes();
		var instancesUUIDs = instances.stream().map(Gene::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			geneResourceApi.deleteGene(instanceUUID);
		}
	}

	private static void deleteMeasure() throws ApiException {
		MeasureResourceApi measureResourceApi = new MeasureResourceApi();
		measureResourceApi.setApiClient(getApiClient());

		var instances = measureResourceApi.getAllMeasures();
		var instancesUUIDs = instances.stream().map(Measure::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			measureResourceApi.deleteMeasure(instanceUUID);
		}
	}

	private static void deleteLibrarySource() throws ApiException {
		LibrarySourceResourceApi librarySourceResourceApi = new LibrarySourceResourceApi();
		librarySourceResourceApi.setApiClient(getApiClient());

		var instances = librarySourceResourceApi.getAllLibrarySources();
		var instancesUUIDs = instances.stream().map(LibrarySource::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			librarySourceResourceApi.deleteLibrarySource(instanceUUID);
		}
	}

	private static void deleteIndividualTreatment() throws ApiException {
		IndividualTreatmentResourceApi individualTreatmentResourceApi = new IndividualTreatmentResourceApi();
		individualTreatmentResourceApi.setApiClient(getApiClient());

		var instances = individualTreatmentResourceApi.getAllIndividualTreatments();
		var instancesUUIDs = instances.stream().map(IndividualTreatment::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			individualTreatmentResourceApi.deleteIndividualTreatment(instanceUUID);
		}
	}

	private static void deleteHistologicalDiagnosis() throws ApiException {
		HistologicalDiagnosisResourceApi histologicalDiagnosisResourceApi = new HistologicalDiagnosisResourceApi();
		histologicalDiagnosisResourceApi.setApiClient(getApiClient());

		var instances = histologicalDiagnosisResourceApi.getAllHistologicalDiagnoses();
		var instancesUUIDs = instances.stream().map(HistologicalDiagnosis::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			histologicalDiagnosisResourceApi.deleteHistologicalDiagnosis(instanceUUID);
		}
	}

	private static void deleteExposure() throws ApiException {
		ExposureResourceApi exposureResourceApi = new ExposureResourceApi();
		exposureResourceApi.setApiClient(getApiClient());

		var instances = exposureResourceApi.getAllExposures();
		var instancesUUIDs = instances.stream().map(Exposure::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			exposureResourceApi.deleteExposure(instanceUUID);
		}
	}

	private static void deleteEthnicity() throws ApiException {
		EthnicityResourceApi ethnicityResourceApi = new EthnicityResourceApi();
		ethnicityResourceApi.setApiClient(getApiClient());

		var instances = ethnicityResourceApi.getAllEthnicities();
		var instancesUUIDs = instances.stream().map(Ethnicity::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			ethnicityResourceApi.deleteEthnicity(instanceUUID);
		}
	}

	private static void deleteDiseases() throws ApiException {
		DiseaseResourceApi diseaseResourceApi = new DiseaseResourceApi();
		diseaseResourceApi.setApiClient(getApiClient());

		var instances = diseaseResourceApi.getAllDiseases();
		var instancesUUIDs = instances.stream().map(Disease::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			diseaseResourceApi.deleteDisease(instanceUUID);
		}
	}


	private static void deleteDiseaseStage() throws ApiException {
		DiseaseStageResourceApi diseaseStageResourceApi = new DiseaseStageResourceApi();
		diseaseStageResourceApi.setApiClient(getApiClient());

		var instances = diseaseStageResourceApi.getAllDiseaseStages();
		var instancesUUIDs = instances.stream().map(DiseaseStage::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			diseaseStageResourceApi.deleteDiseaseStage(instanceUUID);
		}
	}

	private static void deleteDiagnosticMarker() throws ApiException {
		DiagnosticMarkerResourceApi diagnosticMarkerResourceApi = new DiagnosticMarkerResourceApi();
		diagnosticMarkerResourceApi.setApiClient(getApiClient());

		var instances = diagnosticMarkerResourceApi.getAllDiagnosticMarkers();
		var instancesUUIDs = instances.stream().map(DiagnosticMarker::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			diagnosticMarkerResourceApi.deleteDiagnosticMarker(instanceUUID);
		}
	}

	private static void deleteCohortDesign() throws ApiException {
		CohortDesignResourceApi cohortDesignResourceApi = new CohortDesignResourceApi();
		cohortDesignResourceApi.setApiClient(getApiClient());

		var instances = cohortDesignResourceApi.getAllCohortDesigns();
		var instancesUUIDs = instances.stream().map(CohortDesign::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			cohortDesignResourceApi.deleteCohortDesign(instanceUUID);
		}
	}

	private static void deleteBodySite() throws ApiException {
		BodySiteResourceApi bodySiteResourceApi = new BodySiteResourceApi();
		bodySiteResourceApi.setApiClient(getApiClient());

		var instances = bodySiteResourceApi.getAllBodySites();
		var instancesUUIDs = instances.stream().map(BodySite::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			bodySiteResourceApi.deleteBodySite(instanceUUID);
		}
	}

	private static void deleteBiosampleObtentionProcedure() throws ApiException {
		BiosampleObtentionProcedureResourceApi biosampleObtentionProcedureResourceApi = new BiosampleObtentionProcedureResourceApi();
		biosampleObtentionProcedureResourceApi.setApiClient(getApiClient());

		var instances = biosampleObtentionProcedureResourceApi.getAllBiosampleObtentionProcedures();
		var instancesUUIDs = instances.stream().map(BiosampleObtentionProcedure::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			biosampleObtentionProcedureResourceApi.deleteBiosampleObtentionProcedure(instanceUUID);
		}
	}

	private static void deleteBiosampleStatus() throws ApiException {
		BiosampleStatusResourceApi biosampleStatusResourceApi = new BiosampleStatusResourceApi();
		biosampleStatusResourceApi.setApiClient(getApiClient());

		var instances = biosampleStatusResourceApi.getAllBiosampleStatuses();
		var instancesUUIDs = instances.stream().map(BiosampleStatus::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			biosampleStatusResourceApi.deleteBiosampleStatus(instanceUUID);
		}
	}

	/*private static void deleteIndividualToDisease() throws ApiException {
		IndividualToDiseaseResourceApi individualToDiseaseResourceApi = new IndividualToDiseaseResourceApi();
		individualToDiseaseResourceApi.setApiClient(getApiClient());

		var instances = individualToDiseaseResourceApi.getAllIndividualToDiseases();
		var instancesUUIDs = instances.stream().map(BiosampleStatus::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			individualToDiseaseResourceApi.deleteBiosampleStatus(instanceUUID);
		}
	}*/

	private static void deleteFamilyHistory() throws ApiException {
		FamilyHistoryResourceApi familyHistoryResourceApi = new FamilyHistoryResourceApi();
		familyHistoryResourceApi.setApiClient(getApiClient());

		var instances = familyHistoryResourceApi.getAllFamilyHistories();
		var instancesUUIDs = instances.stream().map(FamilyHistory::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			familyHistoryResourceApi.deleteFamilyHistory(instanceUUID);
		}
	}







}
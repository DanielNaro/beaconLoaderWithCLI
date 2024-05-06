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
		//Map<ReadBiosampleStatus, UUID> createdBioSampleStatuses = new HashMap<>();
		Map<ReadObtentionProcedure, UUID> createdBiosampleObtenitionProcedures = new HashMap<>();
		//Map<ReadBiosampleOriginType, BiosampleSampleOrigin> createdBiosampleOrigins = new HashMap<>();
		loadDatasets();
		loadIndividuals(createdIndividuals);
		//loadBiosamples(createdIndividuals, createdBiosamples, createdBioSampleStatuses, createdBiosampleObtenitionProcedures, createdBiosampleOrigins);
	}

	/*private void loadBiosamples(
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
	}*/

	/*private void loadReadBiosample(
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
	}*/

	private void loadIndividuals(Map<String, Individual> createdIndividuals) throws IOException, ApiException {
		try (InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream("./src/main/resources/toLoad/individuals.json"))){
			Gson gson = new Gson();
			var readIndividuals = gson.fromJson(jsonFileInputStream, ReadIndividual[].class);

			IndividualResourceApi individualResourceApi = new IndividualResourceApi();
			individualResourceApi.setApiClient(getApiClient());
			MeasuresItemResourceApi measureResourceApi = new MeasuresItemResourceApi();
			measureResourceApi.setApiClient(getApiClient());
			OntologyTermResourceApi ontologyTermResourceApi = new OntologyTermResourceApi();
			ontologyTermResourceApi.setApiClient(getApiClient());
			Map<String, OntologyTerm> createdSexes = new HashMap<>();
			Map<String, OntologyTerm> createdAssayCodes = new HashMap<>();

			for(ReadIndividual readIndividual: readIndividuals){
				loadReadIndividual(
					individualResourceApi,
					measureResourceApi,
					ontologyTermResourceApi,
					readIndividual,
					createdIndividuals,
					createdSexes,
					createdAssayCodes
				);
			}
		}
	}

	private void loadReadIndividual(
			IndividualResourceApi individualResourceApi,
			MeasuresItemResourceApi measureResourceApi,
			OntologyTermResourceApi ontologyTermResourceApi,
			ReadIndividual readIndividual,
			Map<String, Individual> createdIndividuals,
			Map<String, OntologyTerm> createdSexes,
			Map<String, OntologyTerm> createdAssayCodes,
			Map<String, Procedure> createdProcedures
	) throws ApiException {
		if (!createdSexes.containsKey(readIndividual.getSex().getId())){
			OntologyTerm sexOntologyTerm = ontologyTermResourceApi.createOntologyTerm(readIndividual.getSex().toAPIRepresentation());
			createdSexes.put(readIndividual.getSex().getId(), sexOntologyTerm);
		}

		for (var readMeasure : readIndividual.getMeasures()) {
			if (!createdAssayCodes.containsKey(readMeasure.getAssayCode().getId())) {
				OntologyTerm toCreateAssayCodeToCreate = new OntologyTerm();
				toCreateAssayCodeToCreate.setIdAsProvided(readMeasure.getAssayCode().getId());
				toCreateAssayCodeToCreate.setLabel(readMeasure.getAssayCode().getLabel());

				OntologyTerm createdAssayCodeToCreate = ontologyTermResourceApi.createOntologyTerm(toCreateAssayCodeToCreate);
				createdAssayCodes.put(readMeasure.getAssayCode().getId(), createdAssayCodeToCreate);
			}
		}

		Individual createdIndividual = individualResourceApi.createIndividual(
				readIndividual.getAPIRepresentation(createdSexes, createdAssayCodes)
		);
		readIndividual.getMeasures(measureResourceApi, createdIndividual);
		createdIndividuals.put(readIndividual.getId(), createdIndividual);

		for(var readMeasure: readIndividual.getMeasures()){
			System.out.println(readMeasure);
		}
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
		deleteTreatmentsItem();
		deleteAnalysis();
		deleteRuns();
		deleteQuantities();
		deletePhenotypicFeaturesItems();
		deletePhenotypicConditionsItems();
		deletePedigreesItems();
		deletePathologicalTnmFindingItems();
		deleteModifiersItems();
		deleteMembersItems();
		deleteMeasuresItems();
		deleteMeasurementsItems();
		deleteLocationsItems();
		deleteInterventionsOrProceduresItems();
		deleteGenomicVariants();
		deleteGendersItems();
		deleteExternalReferences();
		deleteExposureItems();
		deleteEvidences();
		deleteEventEthnicities();
		deleteEventDiseases();
		deleteEventDataTypes();
		deleteEventAgeRanges();
		deleteEthnicitiesItems();
		deleteDoseIntervalsItems();
		deleteDiseases();
		deleteDiseasesItems();
		deleteDiseaseConditionsItems();
		deleteDiagnosticMarkersItems();
		deleteDatasets();
		deleteDataUseConditions();
		deleteCollectionEventsItems();
		deleteCohorts();
		deleteCohortDataTypesItems();
		//deleteIndividuals();
		deleteOntologyTerm();
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

	private static void deleteOntologyTerm() throws ApiException { OntologyTermResourceApi ontologyTermResourceApi = new OntologyTermResourceApi();
		ontologyTermResourceApi.setApiClient(getApiClient());

		var instances = ontologyTermResourceApi.getAllOntologyTerms("");
		var instancesUUIDs = instances.stream().map(OntologyTerm::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			ontologyTermResourceApi.deleteOntologyTerm(instanceUUID);
		}
	}

	private static void deleteTreatmentsItem() throws ApiException { TreatmentsItemResourceApi treatmentsItemResourceApi = new TreatmentsItemResourceApi();
		treatmentsItemResourceApi.setApiClient(getApiClient());

		var instances = treatmentsItemResourceApi.getAllTreatmentsItems(false);
		var instancesUUIDs = instances.stream().map(TreatmentsItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			treatmentsItemResourceApi.deleteTreatmentsItem(instanceUUID);
		}
	}

	private static void deleteAnalysis() throws ApiException {
		SequencingBioinformaticsAnalysisResourceApi analysiResourceApi = new SequencingBioinformaticsAnalysisResourceApi();
		analysiResourceApi.setApiClient(getApiClient());

		var instances = analysiResourceApi.getAllSequencingBioinformaticsAnalyses();
		var instancesIDs = instances.stream().map(SequencingBioinformaticsAnalysis::getId).collect(Collectors.toSet());

		for (var instanceID: instancesIDs){
			analysiResourceApi.deleteSequencingBioinformaticsAnalysis(instanceID);
		}
	}
	private static void deleteRuns() throws ApiException { RunResourceApi runResourceApi = new RunResourceApi();
		runResourceApi.setApiClient(getApiClient());

		var instances = runResourceApi.getAllRuns();
		var instancesUUIDs = instances.stream().map(Run::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			runResourceApi.deleteRun(instanceUUID);
		}
	}

	private static void deleteQuantities() throws ApiException { QuantityResourceApi quantitieResourceApi = new QuantityResourceApi();
		quantitieResourceApi.setApiClient(getApiClient());

		var instances = quantitieResourceApi.getAllQuantities();
		var instancesUUIDs = instances.stream().map(Quantity::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			quantitieResourceApi.deleteQuantity(instanceUUID);
		}
	}

	private static void deletePhenotypicFeaturesItems() throws ApiException { PhenotypicFeaturesItemResourceApi phenotypicFeaturesItemResourceApi = new PhenotypicFeaturesItemResourceApi();
		phenotypicFeaturesItemResourceApi.setApiClient(getApiClient());

		var instances = phenotypicFeaturesItemResourceApi.getAllPhenotypicFeaturesItems(false);
		var instancesUUIDs = instances.stream().map(PhenotypicFeaturesItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			phenotypicFeaturesItemResourceApi.deletePhenotypicFeaturesItem(instanceUUID);
		}
	}
	private static void deletePhenotypicConditionsItems() throws ApiException { PhenotypicConditionsItemResourceApi phenotypicConditionsItemResourceApi = new PhenotypicConditionsItemResourceApi();
		phenotypicConditionsItemResourceApi.setApiClient(getApiClient());

		var instances = phenotypicConditionsItemResourceApi.getAllPhenotypicConditionsItems(false);
		var instancesUUIDs = instances.stream().map(PhenotypicConditionsItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			phenotypicConditionsItemResourceApi.deletePhenotypicConditionsItem(instanceUUID);
		}
	}
	private static void deletePedigreesItems() throws ApiException { PedigreesItemResourceApi pedigreesItemResourceApi = new PedigreesItemResourceApi();
		pedigreesItemResourceApi.setApiClient(getApiClient());

		var instances = pedigreesItemResourceApi.getAllPedigreesItems(false);
		var instancesUUIDs = instances.stream().map(PedigreesItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			pedigreesItemResourceApi.deletePedigreesItem(instanceUUID);
		}
	}
	private static void deletePathologicalTnmFindingItems() throws ApiException { PathologicalTnmFindingItemResourceApi pathologicalTnmFindingItemResourceApi = new PathologicalTnmFindingItemResourceApi();
		pathologicalTnmFindingItemResourceApi.setApiClient(getApiClient());

		var instances = pathologicalTnmFindingItemResourceApi.getAllPathologicalTnmFindingItems();
		var instancesUUIDs = instances.stream().map(PathologicalTnmFindingItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			pathologicalTnmFindingItemResourceApi.deletePathologicalTnmFindingItem(instanceUUID);
		}
	}

	private static void deleteModifiersItems() throws ApiException { ModifiersItemResourceApi modifiersItemResourceApi = new ModifiersItemResourceApi();
		modifiersItemResourceApi.setApiClient(getApiClient());

		var instances = modifiersItemResourceApi.getAllModifiersItems();
		var instancesUUIDs = instances.stream().map(ModifiersItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			modifiersItemResourceApi.deleteModifiersItem(instanceUUID);
		}
	}
	private static void deleteMembersItems() throws ApiException { MembersItemResourceApi membersItemResourceApi = new MembersItemResourceApi();
		membersItemResourceApi.setApiClient(getApiClient());

		var instances = membersItemResourceApi.getAllMembersItems();
		var instancesUUIDs = instances.stream().map(MembersItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			membersItemResourceApi.deleteMembersItem(instanceUUID);
		}
	}
	private static void deleteMeasuresItems() throws ApiException { MeasuresItemResourceApi measuresItemResourceApi = new MeasuresItemResourceApi();
		measuresItemResourceApi.setApiClient(getApiClient());

		var instances = measuresItemResourceApi.getAllMeasuresItems();
		var instancesUUIDs = instances.stream().map(MeasuresItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			measuresItemResourceApi.deleteMeasuresItem(instanceUUID);
		}
	}
	private static void deleteMeasurementsItems() throws ApiException { MeasurementsItemResourceApi measurementsItemResourceApi = new MeasurementsItemResourceApi();
		measurementsItemResourceApi.setApiClient(getApiClient());

		var instances = measurementsItemResourceApi.getAllMeasurementsItems();
		var instancesUUIDs = instances.stream().map(MeasurementsItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			measurementsItemResourceApi.deleteMeasurementsItem(instanceUUID);
		}
	}
	private static void deleteLocationsItems() throws ApiException { LocationsItemResourceApi locationsItemResourceApi = new LocationsItemResourceApi();
		locationsItemResourceApi.setApiClient(getApiClient());

		var instances = locationsItemResourceApi.getAllLocationsItems();
		var instancesUUIDs = instances.stream().map(LocationsItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			locationsItemResourceApi.deleteLocationsItem(instanceUUID);
		}
	}
	private static void deleteInterventionsOrProceduresItems() throws ApiException { InterventionsOrProceduresItemResourceApi interventionsOrProceduresItemResourceApi = new InterventionsOrProceduresItemResourceApi();
		interventionsOrProceduresItemResourceApi.setApiClient(getApiClient());

		var instances = interventionsOrProceduresItemResourceApi.getAllInterventionsOrProceduresItems();
		var instancesUUIDs = instances.stream().map(InterventionsOrProceduresItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			interventionsOrProceduresItemResourceApi.deleteInterventionsOrProceduresItem(instanceUUID);
		}
	}

	private static void deleteGenomicVariants() throws ApiException { GenomicVariantResourceApi genomicVariantResourceApi = new GenomicVariantResourceApi();
		genomicVariantResourceApi.setApiClient(getApiClient());

		var instances = genomicVariantResourceApi.getAllGenomicVariants();
		var instancesUUIDs = instances.stream().map(GenomicVariant::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			genomicVariantResourceApi.deleteGenomicVariant(instanceUUID);
		}
	}
	private static void deleteGendersItems() throws ApiException { GendersItemResourceApi gendersItemResourceApi = new GendersItemResourceApi();
		gendersItemResourceApi.setApiClient(getApiClient());

		var instances = gendersItemResourceApi.getAllGendersItems();
		var instancesUUIDs = instances.stream().map(GendersItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			gendersItemResourceApi.deleteGendersItem(instanceUUID);
		}
	}
	private static void deleteExternalReferences() throws ApiException { ExternalReferenceResourceApi externalReferenceResourceApi = new ExternalReferenceResourceApi();
		externalReferenceResourceApi.setApiClient(getApiClient());

		var instances = externalReferenceResourceApi.getAllExternalReferences();
		var instancesUUIDs = instances.stream().map(ExternalReference::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			externalReferenceResourceApi.deleteExternalReference(instanceUUID);
		}
	}
	private static void deleteExposureItems() throws ApiException { ExposuresItemResourceApi exposureItemResourceApi = new ExposuresItemResourceApi();
		exposureItemResourceApi.setApiClient(getApiClient());

		var instances = exposureItemResourceApi.getAllExposuresItems();
		var instancesUUIDs = instances.stream().map(ExposuresItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			exposureItemResourceApi.deleteExposuresItem(instanceUUID);
		}
	}

	private static void deleteEvidences() throws ApiException { EvidenceResourceApi evidenceResourceApi = new EvidenceResourceApi();
		evidenceResourceApi.setApiClient(getApiClient());

		var instances = evidenceResourceApi.getAllEvidences();
		var instancesUUIDs = instances.stream().map(Evidence::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			evidenceResourceApi.deleteEvidence(instanceUUID);
		}
	}

	private static void deleteEventEthnicities() throws ApiException { EventEthnicitiesResourceApi eventEthnicitieResourceApi = new EventEthnicitiesResourceApi();
		eventEthnicitieResourceApi.setApiClient(getApiClient());

		var instances = eventEthnicitieResourceApi.getAllEventEthnicities();
		var instancesUUIDs = instances.stream().map(EventEthnicities::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			eventEthnicitieResourceApi.deleteEventEthnicities(instanceUUID);
		}
	}
	private static void deleteEventDiseases() throws ApiException { EventDiseasesResourceApi eventDiseaseResourceApi = new EventDiseasesResourceApi();
		eventDiseaseResourceApi.setApiClient(getApiClient());

		var instances = eventDiseaseResourceApi.getAllEventDiseases();
		var instancesUUIDs = instances.stream().map(EventDiseases::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			eventDiseaseResourceApi.deleteEventDiseases(instanceUUID);
		}
	}
	private static void deleteEventDataTypes() throws ApiException { EventDataTypesResourceApi eventDataTypeResourceApi = new EventDataTypesResourceApi();
		eventDataTypeResourceApi.setApiClient(getApiClient());

		var instances = eventDataTypeResourceApi.getAllEventDataTypes();
		var instancesUUIDs = instances.stream().map(EventDataTypes::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			eventDataTypeResourceApi.deleteEventDataTypes(instanceUUID);
		}
	}
	private static void deleteEventAgeRanges() throws ApiException { EventAgeRangeResourceApi eventAgeRangeResourceApi = new EventAgeRangeResourceApi();
		eventAgeRangeResourceApi.setApiClient(getApiClient());

		var instances = eventAgeRangeResourceApi.getAllEventAgeRanges();
		var instancesUUIDs = instances.stream().map(EventAgeRange::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			eventAgeRangeResourceApi.deleteEventAgeRange(instanceUUID);
		}
	}
	private static void deleteEthnicitiesItems() throws ApiException { EthnicitiesItemResourceApi ethnicitiesItemResourceApi = new EthnicitiesItemResourceApi();
		ethnicitiesItemResourceApi.setApiClient(getApiClient());

		var instances = ethnicitiesItemResourceApi.getAllEthnicitiesItems();
		var instancesUUIDs = instances.stream().map(EthnicitiesItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			ethnicitiesItemResourceApi.deleteEthnicitiesItem(instanceUUID);
		}
	}
	private static void deleteDoseIntervalsItems() throws ApiException { DoseIntervalsItemResourceApi doseIntervalsItemResourceApi = new DoseIntervalsItemResourceApi();
		doseIntervalsItemResourceApi.setApiClient(getApiClient());

		var instances = doseIntervalsItemResourceApi.getAllDoseIntervalsItems();
		var instancesUUIDs = instances.stream().map(DoseIntervalsItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			doseIntervalsItemResourceApi.deleteDoseIntervalsItem(instanceUUID);
		}
	}
	private static void deleteDiseases() throws ApiException { DiseaseResourceApi diseaseResourceApi = new DiseaseResourceApi();
		diseaseResourceApi.setApiClient(getApiClient());

		var instances = diseaseResourceApi.getAllDiseases();
		var instancesUUIDs = instances.stream().map(Disease::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			diseaseResourceApi.deleteDisease(instanceUUID);
		}
	}
	private static void deleteDiseasesItems() throws ApiException { DiseasesItemResourceApi diseasesItemResourceApi = new DiseasesItemResourceApi();
		diseasesItemResourceApi.setApiClient(getApiClient());

		var instances = diseasesItemResourceApi.getAllDiseasesItems();
		var instancesUUIDs = instances.stream().map(DiseasesItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			diseasesItemResourceApi.deleteDiseasesItem(instanceUUID);
		}
	}
	private static void deleteDiseaseConditionsItems() throws ApiException { DiseaseConditionsItemResourceApi diseaseConditionsItemResourceApi = new DiseaseConditionsItemResourceApi();
		diseaseConditionsItemResourceApi.setApiClient(getApiClient());

		var instances = diseaseConditionsItemResourceApi.getAllDiseaseConditionsItems();
		var instancesUUIDs = instances.stream().map(DiseaseConditionsItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			diseaseConditionsItemResourceApi.deleteDiseaseConditionsItem(instanceUUID);
		}
	}
	private static void deleteDiagnosticMarkersItems() throws ApiException { DiagnosticMarkersItemResourceApi diagnosticMarkersItemResourceApi = new DiagnosticMarkersItemResourceApi();
		diagnosticMarkersItemResourceApi.setApiClient(getApiClient());

		var instances = diagnosticMarkersItemResourceApi.getAllDiagnosticMarkersItems();
		var instancesUUIDs = instances.stream().map(DiagnosticMarkersItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			diagnosticMarkersItemResourceApi.deleteDiagnosticMarkersItem(instanceUUID);
		}
	}
	private static void deleteDatasets() throws ApiException { DatasetResourceApi datasetResourceApi = new DatasetResourceApi();
		datasetResourceApi.setApiClient(getApiClient());

		var instances = datasetResourceApi.getAllDatasets();
		var instancesUUIDs = instances.stream().map(Dataset::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			datasetResourceApi.deleteDataset(instanceUUID);
		}
	}
	private static void deleteDataUseConditions() throws ApiException { DataUseConditionsResourceApi dataUseConditionResourceApi = new DataUseConditionsResourceApi();
		dataUseConditionResourceApi.setApiClient(getApiClient());

		var instances = dataUseConditionResourceApi.getAllDataUseConditions();
		var instancesUUIDs = instances.stream().map(DataUseConditions::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			dataUseConditionResourceApi.deleteDataUseConditions(instanceUUID);
		}
	}
	private static void deleteCollectionEventsItems() throws ApiException { CollectionEventsItemResourceApi collectionEventsItemResourceApi = new CollectionEventsItemResourceApi();
		collectionEventsItemResourceApi.setApiClient(getApiClient());

		var instances = collectionEventsItemResourceApi.getAllCollectionEventsItems();
		var instancesUUIDs = instances.stream().map(CollectionEventsItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			collectionEventsItemResourceApi.deleteCollectionEventsItem(instanceUUID);
		}
	}
	private static void deleteCohorts() throws ApiException { CohortResourceApi cohortResourceApi = new CohortResourceApi();
		cohortResourceApi.setApiClient(getApiClient());

		var instances = cohortResourceApi.getAllCohorts();
		var instancesUUIDs = instances.stream().map(Cohort::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			cohortResourceApi.deleteCohort(instanceUUID);
		}
	}
	private static void deleteCohortDataTypesItems() throws ApiException { CohortDataTypesItemResourceApi cohortDataTypesItemResourceApi = new CohortDataTypesItemResourceApi();
		cohortDataTypesItemResourceApi.setApiClient(getApiClient());

		var instances = cohortDataTypesItemResourceApi.getAllCohortDataTypesItems();
		var instancesUUIDs = instances.stream().map(CohortDataTypesItem::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			cohortDataTypesItemResourceApi.deleteCohortDataTypesItem(instanceUUID);
		}
	}

	private static void deleteIndividuals() throws ApiException {
		IndividualResourceApi individualResourceApi = new IndividualResourceApi();
		individualResourceApi.setApiClient(getApiClient());

		var instances = individualResourceApi.getAllIndividuals("",true);
		var instancesUUIDs = instances.stream().map(Individual::getId).collect(Collectors.toSet());

		for (var instanceUUID: instancesUUIDs){
			individualResourceApi.deleteIndividual(instanceUUID);
		}
	}
}
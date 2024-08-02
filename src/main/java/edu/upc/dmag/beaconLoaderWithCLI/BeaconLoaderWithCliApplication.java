package edu.upc.dmag.beaconLoaderWithCLI;


import com.google.gson.Gson;
import com.google.gson.internal.bind.util.ISO8601Utils;
import edu.upc.dmag.ToLoad.DatasetsSchema;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Dataset;
import edu.upc.dmag.beaconLoaderWithCLI.entities.DatasetRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.text.ParseException;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class BeaconLoaderWithCliApplication implements CommandLineRunner {

	private static Logger LOG = LoggerFactory
			.getLogger(BeaconLoaderWithCliApplication.class);
	private final DatasetRepository datasetRepository;

	public BeaconLoaderWithCliApplication(DatasetRepository datasetRepository) {
		this.datasetRepository = datasetRepository;
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

	private void loadReadDataset(DatasetsSchema readDataset) throws ParseException {
		var dataset = new Dataset();
		dataset.setId(readDataset.getId());
		dataset.setName(readDataset.getName());
		dataset.setDescription(readDataset.getDescription());
		dataset.setVersion(readDataset.getVersion());
		dataset.setExternalUrl(readDataset.getExternalUrl());
		dataset.setCreateDateTime(ZonedDateTime.parse(readDataset.getCreateDateTime()));
		dataset.setUpdateDateTime(ZonedDateTime.parse(readDataset.getUpdateDateTime()));
		//readDataset.getDataUseConditions()
		//readDataset.getAdditionalProperties()
		//readDataset.getInfo()
		datasetRepository.save(dataset);


		dataset.setId("bla");
		dataset.setName("blaName");
		dataset.setDescription("blaDescription");
		datasetRepository.save(dataset);
	}
}
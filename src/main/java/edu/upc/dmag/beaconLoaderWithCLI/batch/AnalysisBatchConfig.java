package edu.upc.dmag.beaconLoaderWithCLI.batch;

import com.google.gson.Gson;
import edu.upc.dmag.ToLoad.AnalysesSchema;
import edu.upc.dmag.beaconLoaderWithCLI.entities.Analysis;
import edu.upc.dmag.beaconLoaderWithCLI.entities.AnalysisRepository;
import edu.upc.dmag.beaconLoaderWithCLI.entities.RunRepository;
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
import java.util.Iterator;

@Configuration
public class AnalysisBatchConfig {

    private final AnalysisRepository analysisRepository;
    private final RunRepository runRepository;

    public AnalysisBatchConfig(AnalysisRepository analysisRepository,
                               RunRepository runRepository) {
        this.analysisRepository = analysisRepository;
        this.runRepository = runRepository;
    }

    @Bean
    public Job loadAnalysesJob(JobRepository jobRepository, Step loadAnalysesStep) {
        return new JobBuilder("loadAnalysesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(loadAnalysesStep)
                .build();
    }

    @Bean
    public Step loadAnalysesStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  ItemReader<AnalysesSchema> analysisReader,
                                  ItemProcessor<AnalysesSchema, Analysis> analysisProcessor,
                                  ItemWriter<Analysis> analysisWriter) {
        return new StepBuilder("loadAnalysesStep", jobRepository)
                .<AnalysesSchema, Analysis>chunk(10, transactionManager)
                .reader(analysisReader)
                .processor(analysisProcessor)
                .writer(analysisWriter)
                .build();
    }

    @Bean
    public ItemReader<AnalysesSchema> analysisReader() {
        return new AnalysisJsonItemReader("./src/main/resources/toLoad/analyses.json");
    }

    @Bean
    public ItemProcessor<AnalysesSchema, Analysis> analysisProcessor() {
        return readAnalysis -> {
            Analysis analysis = new Analysis();
            analysis.setId(readAnalysis.getId());
            analysis.setAligner(readAnalysis.getAligner());
            analysis.setAnalysisDate(Date.valueOf(readAnalysis.getAnalysisDate()));
            analysis.setPipelineName(readAnalysis.getPipelineName());
            analysis.setPipelineRef(readAnalysis.getPipelineRef());
            analysis.setVariantCaller(readAnalysis.getVariantCaller());
            analysis.setRun(runRepository.getReferenceById(readAnalysis.getRunId()));
            return analysis;
        };
    }

    @Bean
    public ItemWriter<Analysis> analysisWriter() {
        return analyses -> {
            for (Analysis analysis : analyses) {
                analysisRepository.save(analysis);
            }
        };
    }

    /**
     * Custom ItemReader that reads AnalysesSchema objects from a JSON array file.
     */
    public static class AnalysisJsonItemReader implements ItemReader<AnalysesSchema> {
        private final String filePath;
        private Iterator<AnalysesSchema> analysisIterator;
        private boolean initialized = false;

        public AnalysisJsonItemReader(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public AnalysesSchema read() {
            if (!initialized) {
                initialize();
            }
            if (analysisIterator != null && analysisIterator.hasNext()) {
                return analysisIterator.next();
            }
            return null;
        }

        private void initialize() {
            try {
                InputStreamReader jsonFileInputStream = new InputStreamReader(new FileInputStream(filePath));
                Gson gson = new Gson();
                AnalysesSchema[] readAnalyses = gson.fromJson(jsonFileInputStream, AnalysesSchema[].class);
                analysisIterator = Arrays.asList(readAnalyses).iterator();
                initialized = true;
            } catch (IOException e) {
                throw new RuntimeException("Error reading analyses JSON file: " + filePath, e);
            }
        }
    }
}


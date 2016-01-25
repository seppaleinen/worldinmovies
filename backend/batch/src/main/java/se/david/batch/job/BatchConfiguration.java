package se.david.batch.job;

import lombok.extern.java.Log;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@Log
public class BatchConfiguration {
    // tag::jobstep[]
    @Bean
    public Job importNewCountries(JobBuilderFactory jobs,
                                  @Qualifier(value = "countryStep") Step s1,
                                  @Qualifier(value = "movieSteps") Step s2,
                                  JobExecutionListener listener) {
        return jobs.get("Importer")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(s1)
                .next(s2)
                .end()
                .build();
    }

}

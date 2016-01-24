package se.david.batch.countries;

import lombok.extern.java.Log;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import se.david.batch.countries.beans.CountryItemWriter;
import se.david.batch.countries.beans.CountryProcessor;
import se.david.commons.Country;

import javax.sql.DataSource;
import java.util.logging.Level;

@Configuration
@EnableBatchProcessing
@Log
public class CountryBatchConfiguration {
    @Bean
    public ItemReader<Country> reader() {
        FlatFileItemReader<Country> reader = new FlatFileItemReader<>();

        /**
         * http://data.okfn.org/data/core/country-list/r/data.csv
         */
        reader.setResource(new ClassPathResource("countries.csv"));
        reader.setLineMapper(new DefaultLineMapper<Country>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] { "name", "code" });
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Country>() {{
                setTargetType(Country.class);
            }});
        }});
        return reader;
    }

    @Bean
    public ItemProcessor<Country, Country> processor() {
        return new CountryProcessor();
    }

    @Bean
    public ItemWriter<Country> writer(DataSource dataSource) {
        return new CountryItemWriter();
    }
    // end::readerwriterprocessor[]

    // tag::jobstep[]
    @Bean
    public Job importUserJob(JobBuilderFactory jobs, Step s1, JobExecutionListener listener) {
        return jobs.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(s1)
                .end()
                .build();
    }

    @Bean
    public Step step1(StepBuilderFactory stepBuilderFactory,
                      ItemReader<Country> reader,
                      ItemWriter<Country> writer,
                      ItemProcessor<Country, Country> processor) {
        return stepBuilderFactory.get("step1")
                .<Country, Country> chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
    // end::jobstep[]
}

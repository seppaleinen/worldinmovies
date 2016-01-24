package se.david.batch.job.country;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import se.david.batch.job.country.beans.CountryItemWriter;
import se.david.batch.job.country.beans.CountryProcessor;
import se.david.commons.Country;

import javax.sql.DataSource;

@Service
public class CountriesStep {
    @Autowired
    private CountryProcessor countryProcessor;
    @Autowired
    private CountryItemWriter countryItemWriter;

    @Bean(name = "countryReader")
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
        return countryProcessor;
    }

    @Bean
    public ItemWriter<Country> writer(DataSource dataSource) {
        return countryItemWriter;
    }
    // end::readerwriterprocessor[]


    @Bean(name = "countryStep")
    public Step convertNewCountries(StepBuilderFactory stepBuilderFactory,
                                    @Qualifier(value = "countryReader") ItemReader<Country> reader,
                                    ItemWriter<Country> writer,
                                    ItemProcessor<Country, Country> processor) {
        return stepBuilderFactory.get("convertNewCountries")
                .<Country, Country> chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
    // end::jobstep[]
}

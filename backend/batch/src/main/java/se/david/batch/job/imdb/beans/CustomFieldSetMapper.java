package se.david.batch.job.imdb.beans;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;
import se.david.commons.Movie;

public class CustomFieldSetMapper implements FieldSetMapper<Movie> {
    @Override
    public Movie mapFieldSet(FieldSet fieldSet) throws BindException {
        Movie entry = null;

        if(fieldSet != null && fieldSet.getFieldCount() > 1) {
            entry = new Movie();

            entry.setName(fieldSet.readString(0));
            entry.setYear(fieldSet.readString(1));
        }

        return entry;
    }
}
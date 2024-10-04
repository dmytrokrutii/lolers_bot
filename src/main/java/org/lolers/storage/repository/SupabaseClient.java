package org.lolers.storage.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import kong.unirest.ContentType;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.lolers.storage.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Singleton
public class SupabaseClient<T extends Entity> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SupabaseClient.class);
    private static final String EQ_FILTER = "eq.%s";
    private static final String API_HEADER = "apikey";

    private final String API_KEY;
    private final String SUPABASE_URL;

    @Inject
    public SupabaseClient(@Named("db.url") String url, @Named("db.key") String key) {
        this.SUPABASE_URL = url;
        this.API_KEY = key;
    }

    public List<T> get(Table table, Class<T[]> entityClass) {
        LOGGER.info("getEntities:: fetch records from table: {}", table.getValue());
        var response = Unirest.get(SUPABASE_URL + table.getValue())
                .header(API_HEADER, API_KEY)
                .asObject(entityClass);
        if (response.isSuccess()) {
            return List.of(response.getBody());
        } else {
            LOGGER.error("getEntities:: status code: {}, body: {}", response.getStatus(), response.getBody());
            throw new UnirestException("getEntities:: status code: " + response.getStatus());
        }
    }

    public T getById(Table table, long id, Class<T[]> entityClass) {
        LOGGER.info("getEntity:: get record by id: {}, table {}", id, table.getValue());
        var response = Unirest.get(SUPABASE_URL + table.getValue())
                .header(API_HEADER, API_KEY)
                .queryString("id", String.format(EQ_FILTER, id))
                .asObject(entityClass);
        if (response.isSuccess()) {
            var body = response.getBody();
            if (body != null && body.length > 0) {
                return body[0];
            } else {
                LOGGER.error("getEntity:: no record found for id: {}, table {}", id, table.getValue());
                throw new UnirestException("getEntity:: no record found for id: " + id);
            }
        } else {
            LOGGER.error("getEntity:: status code: {}, body: {}, table {}", response.getStatus(), response.getBody(), table.getValue());
            throw new UnirestException("getEntity:: status code: " + response.getStatus());
        }
    }

    public T getBy(Table table, Map<String, String> filter, Class<T[]> entityClass) {
        LOGGER.info("getEntity:: get record by filter from table {}", table.getValue());

        var request = Unirest.get(SUPABASE_URL + table.getValue()).header(API_HEADER, API_KEY);
        filter.forEach((key, value) -> {
            if (key != null && value != null) {
                request.queryString(key, String.format(EQ_FILTER, value));
            }
        });
        var response = request.asObject(entityClass);
        if (response.isSuccess()) {
            var body = response.getBody();
            if (body != null && body.length > 0) {
                return body[0];
            } else {
                LOGGER.error("getBy:: no record found for filter, table {}", table.getValue());
                throw new UnirestException("getEntity:: no record found for filter");
            }
        } else {
            LOGGER.error("getBy:: status code: {}, body: {}, table {}", response.getStatus(), response.getBody(), table.getValue());
            throw new UnirestException("getEntity:: status code: " + response.getStatus());
        }
    }

    public void update(Table table, T entity) {
        LOGGER.info("updateEntity:: update record by id: {}", entity.id());
        var response = Unirest.put(SUPABASE_URL + table.getValue())
                .header(API_HEADER, API_KEY)
                .queryString("id", String.format(EQ_FILTER, entity.id()))
                .body(entity)
                .contentType(ContentType.APPLICATION_JSON.toString())
                .asEmpty();
        if (!response.isSuccess()) {
            LOGGER.error("updateEntity:: status code: {}, table {}", response.getStatus(), table.getValue());
            throw new UnirestException("updateEntity:: status code: " + response.getStatus());
        }
    }

    public void bulkSave(Table table, List<T> entities) {
        LOGGER.info("bulkSaveEntities:: save bulk records to table: {}", table.getValue());
        var response = Unirest.post(SUPABASE_URL + table.getValue())
                .header(API_HEADER, API_KEY)
                .body(entities)
                .contentType(ContentType.APPLICATION_JSON.toString())
                .asEmpty();
        if (!response.isSuccess()) {
            LOGGER.error("bulkSaveEntities:: status code: {}, table {}", response.getStatus(), table.getValue());
            throw new UnirestException("bulkSaveEntities:: status code: " + response.getStatus());
        }
    }
}

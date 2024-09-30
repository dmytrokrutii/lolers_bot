package org.lolers.storage.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.lolers.storage.entity.Rating;

import java.util.List;

@Singleton
public class RatingRepository {
    private final SupabaseClient<Rating> ratingClient;

    @Inject
    public RatingRepository(SupabaseClient<Rating> ratingClient) {
        this.ratingClient = ratingClient;
    }

    public void update(Rating rating) {
        ratingClient.update(Table.RATING, rating);
    }

    public Rating getById(long id) {
        return ratingClient.getById(Table.RATING, id, Rating[].class);
    }

    public List<Rating> getAll() {
        return ratingClient.get(Table.RATING, Rating[].class);
    }
}

package org.lolers.service;

import org.lolers.dto.RatingPayload;
import org.lolers.storage.entity.Rating;

import java.util.List;

public interface RatingService {

    void updateRating(RatingPayload payload);

    void updateRating(long id, String payload);

    List<Rating> getRatings();

    Rating getRatingByUserId(long userId);
}

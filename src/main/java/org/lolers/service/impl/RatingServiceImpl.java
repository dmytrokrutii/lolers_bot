package org.lolers.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.lolers.dto.RatingPayload;
import org.lolers.service.RatingService;
import org.lolers.storage.MessageStorage;
import org.lolers.storage.entity.Rating;
import org.lolers.storage.repository.RatingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class RatingServiceImpl implements RatingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RatingServiceImpl.class);
    private static final String CLOWN = "\uD83E\uDD21";
    private static final String LIKE = "\uD83D\uDC4D";
    private static final String FIRE = "\uD83D\uDD25";
    private static final String HEART = "❤";

    private final MessageStorage messageStorage;
    private final RatingRepository ratingRepository;

    @Inject
    public RatingServiceImpl(MessageStorage messageStorage, RatingRepository ratingRepository) {
        this.messageStorage = messageStorage;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public void updateRating(RatingPayload payload) {
        var userId = messageStorage.getUserIdByMessageId(payload.messageId(), payload.chatId());
        if (userId == null) {
            LOGGER.error("updateRating:: message not found, id: {}, chatId: {}", payload.messageId(), payload.chatId());
            return;
        }
        if (payload.userFrom() == userId) {
            return;
        }
        var rating = ratingRepository.getById(userId);
        if (payload.oldReaction().isEmpty()) {
            var reaction = (ReactionTypeEmoji) payload.newReaction().get(0);
            if (reaction.getEmoji().equals(HEART) || reaction.getEmoji().equals(LIKE) || reaction.getEmoji().equals(FIRE)) {
                rating = new Rating(rating.id(), rating.powerCounter() + 1, rating.clownCounter());
                ratingRepository.update(rating);
            } else if (reaction.getEmoji().equals(CLOWN)) {
                rating = new Rating(rating.id(), rating.powerCounter(), rating.clownCounter() + 1);
                ratingRepository.update(rating);
            }
        } else {
            // Collect old reactions and new reactions into sets
            var oldReactions = payload.oldReaction().stream()
                    .map(reaction -> ((ReactionTypeEmoji) reaction).getEmoji())
                    .collect(Collectors.toSet());
            var newReactions = payload.newReaction().stream()
                    .map(reaction -> ((ReactionTypeEmoji) reaction).getEmoji())
                    .collect(Collectors.toSet());

            // Find added and removed reactions
            var addedReactions = new HashSet<>(newReactions);
            addedReactions.removeAll(oldReactions);

            var removedReactions = new HashSet<>(oldReactions);
            removedReactions.removeAll(newReactions);

            // Update rating based on added reactions
            for (String reaction : addedReactions) {
                if (reaction.equals(HEART) || reaction.equals(LIKE)) {
                    rating = new Rating(rating.id(), rating.powerCounter() + 1, rating.clownCounter());
                } else if (reaction.equals(CLOWN)) {
                    rating = new Rating(rating.id(), rating.powerCounter(), rating.clownCounter() + 1);
                }
            }
            // Update rating based on removed reactions
            for (String reaction : removedReactions) {
                if (reaction.equals(HEART) || reaction.equals(LIKE)) {
                    rating = new Rating(rating.id(), rating.powerCounter() - 1, rating.clownCounter());
                } else if (reaction.equals(CLOWN)) {
                    rating = new Rating(rating.id(), rating.powerCounter(), rating.clownCounter() - 1);
                }
            }

            // Save the updated rating to the repository
            ratingRepository.update(rating);
        }
    }

    @Override
    public List<Rating> getRatings() {
        return ratingRepository.getAll();
    }

    @Override
    public Rating getRatingByUserId(long userId) {
        return ratingRepository.getById(userId);
    }
}

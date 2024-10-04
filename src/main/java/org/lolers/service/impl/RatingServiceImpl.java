package org.lolers.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
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
    private static final String POOP = "\uD83D\uDCA9";
    private static final String FIRE = "\uD83D\uDD25";
    private static final String HEART = "❤";
    private static final String REPLY_HEART = "❤️";
    private static final String PARENTHESIS = ")";

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
            var reaction = (ReactionTypeEmoji) payload.newReaction().getFirst();
            modifyNewReaction(reaction.getEmoji(), rating);
        } else {
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

            // Update rating based on added and removed reactions
            addedReactions.forEach(reaction -> modifyNewReaction(reaction, rating));
            removedReactions.forEach(reaction -> modifyRemovedReaction(reaction, rating));
        }
    }

    private void modifyNewReaction(String reaction, Rating rating) {
        modifyReaction(reaction, rating, true);
    }

    private void modifyRemovedReaction(String reaction, Rating rating) {
        modifyReaction(reaction, rating, false);
    }

    private void modifyReaction(String reaction, Rating rating, boolean isIncrementing) {
        var updatedRating = switch (reaction) {
            case HEART, FIRE -> rating.withPowerCounter(rating.powerCounter() + (isIncrementing ? 1 : -1));
            case CLOWN -> rating.withClownCounter(rating.clownCounter() + (isIncrementing ? 1 : -1));
            case POOP -> rating.withPowerCounter(rating.powerCounter() + (isIncrementing ? -1 : 1));
            default -> rating;
        };

        if (!updatedRating.equals(rating)) {
            ratingRepository.update(updatedRating);
        }
    }

    @Override
    public void updateRating(long id, String payload) {
        Rating rating;
        if (StringUtils.containsOnly(payload, PARENTHESIS) || payload.equals(REPLY_HEART) || payload.equals(HEART)) {
            rating = ratingRepository.getById(id);
            ratingRepository.update(rating.withPowerCounter(rating.powerCounter() + 1));
        } else if (payload.equals(CLOWN)) {
            rating = ratingRepository.getById(id);
            ratingRepository.update(rating.withClownCounter(rating.clownCounter() + 1));
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

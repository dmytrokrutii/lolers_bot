package org.lolers.dto;

import org.telegram.telegrambots.meta.api.objects.reactions.ReactionType;

import java.util.List;

public record RatingPayload(int messageId, long chatId, long userFrom, List<ReactionType> newReaction,
                            List<ReactionType> oldReaction) {
}

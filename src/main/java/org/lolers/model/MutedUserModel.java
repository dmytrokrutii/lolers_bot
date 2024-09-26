package org.lolers.model;

public record MutedUserModel(User user, int messageId, long chatId, int muteDurationMinutes, long endTime,
                             boolean muted) {
}
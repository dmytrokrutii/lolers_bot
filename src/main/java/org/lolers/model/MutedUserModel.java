package org.lolers.model;

public record MutedUserModel(User user, int messageId, int muteDurationMinutes, long endTime, boolean muted) {
}
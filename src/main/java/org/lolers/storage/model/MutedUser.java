package org.lolers.storage.model;

import org.lolers.storage.entity.User;

public record MutedUser(User user, int messageId, long chatId, int muteDurationMinutes, long endTime,
                        boolean muted) {
}

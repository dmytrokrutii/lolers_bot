package org.lolers.dto;

import org.lolers.storage.entity.User;

public record InitMutePollPayload(long chatId, int messageId, User user, int duration, boolean selfMute) {
}

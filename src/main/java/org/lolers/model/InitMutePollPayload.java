package org.lolers.model;

public record InitMutePollPayload(long chatId, int messageId, User user, int duration, boolean selfMute) {
}

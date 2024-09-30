package org.lolers.storage.entity;

public record Message(long id, long chatId, long userId) implements Entity {
}

package org.lolers.storage.entity;

public record Rating(long id, int power_counter, int clown_counter) implements Entity {
}

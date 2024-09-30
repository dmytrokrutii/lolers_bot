package org.lolers.storage.entity;

public record Rating(long id, int powerCounter, int clownCounter) implements Entity {
}

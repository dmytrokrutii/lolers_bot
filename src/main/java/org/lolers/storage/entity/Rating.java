package org.lolers.storage.entity;

public record Rating(long id, int powerCounter, int clownCounter) implements Entity {

    public Rating withPowerCounter(int newPowerCounter) {
        return new Rating(this.id, newPowerCounter, this.clownCounter);
    }

    public Rating withClownCounter(int newClownCounter) {
        return new Rating(this.id, this.powerCounter, newClownCounter);
    }
}

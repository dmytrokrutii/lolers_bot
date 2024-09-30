package org.lolers.storage.repository;

public enum Table {
    USERS("users"),
    RATING("rating"),
    MESSAGES("messages");

    private final String value;

    Table(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

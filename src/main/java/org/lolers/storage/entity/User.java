package org.lolers.storage.entity;

import java.util.Date;

public record User(long id, String name, String accusativeName, String tag, Date birthDate) implements Entity {
}

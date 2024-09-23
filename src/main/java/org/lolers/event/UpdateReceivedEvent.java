package org.lolers.event;

import org.telegram.telegrambots.meta.api.objects.Update;

public record UpdateReceivedEvent(Update update) {
}

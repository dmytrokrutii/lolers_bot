package org.lolers.infrastructure;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.lolers.event.UpdateReceivedEvent;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;

@Singleton
public class Bot extends TelegramWebhookBot {
    private static final String USERNAME_ENV = "username";
    private static final String TOKEN_ENV = "token";

    private final Provider<EventBus> bus;

    @Inject
    public Bot(Provider<EventBus> bus) {
        super(System.getenv(TOKEN_ENV));
        this.bus = bus;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        var busInstance = bus.get();
        busInstance.postEvent(new UpdateReceivedEvent(update));
        return null;
    }

    @Override
    public String getBotUsername() {
        return System.getenv(USERNAME_ENV);
    }

    @Override
    public String getBotPath() {
        return LolersApi.WEBHOOK;
    }
}

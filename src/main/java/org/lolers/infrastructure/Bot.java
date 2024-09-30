package org.lolers.infrastructure;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.lolers.event.UpdateReceivedEvent;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.google.inject.name.Named;
import javax.inject.Singleton;

@Singleton
public class Bot extends TelegramWebhookBot {
    private final String USERNAME;

    private final Provider<EventBus> bus;

    @Inject
    public Bot(Provider<EventBus> bus, @Named("bot.username") String username, @Named("bot.token") String token) {
        super(token);
        this.USERNAME = username;
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
        return this.USERNAME;
    }

    @Override
    public String getBotPath() {
        return LolersApi.WEBHOOK;
    }
}

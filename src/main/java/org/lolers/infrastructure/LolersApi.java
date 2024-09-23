package org.lolers.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.javalin.Javalin;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;

@Singleton
public class LolersApi {
    public static final String WEBHOOK = "webhook";

    private final Bot telegramBot;

    @Inject
    public LolersApi(Bot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void start() {
        var app = Javalin.create().start(8443);

        app.post("/" + WEBHOOK, ctx -> {
            try {
                var update = new ObjectMapper().readValue(ctx.body(), Update.class);
                telegramBot.onWebhookUpdateReceived(update);
            } finally {
                ctx.status(200);
            }
        });

        app.get("/hello", ctx -> {
            ctx.status(200);
        });
    }
}

package org.lolers.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.lolers.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Random;

@Singleton
public class FlipCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlipCommand.class);
    private static final String[] COIN = {"Аверс", "Реверс"};
    private final Provider<MessageService> provider;
    private final Random random;

    @Inject
    public FlipCommand(Provider<MessageService> provider) {
        this.provider = provider;
        this.random = new Random();
    }

    @Override
    public void execute(Update update) {
        LOGGER.info("execute:: start executing flipCommand");
        try {
            var msgId = update.getMessage().getMessageId();
            var msg = COIN[random.nextInt(COIN.length)];
            provider.get().replyOnMessage(update.getMessage().getChatId(), msgId, msg, false);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            provider.get().replyOnMessage(update.getMessage().getChatId(), update.getMessage().getMessageId(), FAILED_COMMAND_MESSAGE, true);
        }
    }

    @Override
    public String getInvoker() {
        return "/flip";
    }
}

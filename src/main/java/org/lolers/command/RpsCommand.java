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
public class RpsCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpsCommand.class);
    private static final String[] MOVE = {"\uD83E\uDEA8", "✂️", "\uD83D\uDCC4"};
    private final Provider<MessageService> provider;
    private final Random random;

    @Inject
    public RpsCommand(Provider<MessageService> provider) {
        this.provider = provider;
        this.random = new Random();
    }

    @Override
    public void execute(Update update) {
        LOGGER.info("execute:: start executing rpsCommand");
        try {
            var msgId = update.getMessage().getMessageId();
            var msg = MOVE[random.nextInt(MOVE.length)];
            provider.get().replyOnMessage(update.getMessage().getChatId(), msgId, msg, false);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            provider.get().replyOnMessage(update.getMessage().getChatId(), update.getMessage().getMessageId(), FAILED_COMMAND_MESSAGE, true);
        }
    }

    @Override
    public String getInvoker() {
        return "/rps";
    }
}

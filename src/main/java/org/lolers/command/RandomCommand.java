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
public class RandomCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomCommand.class);
    private static final String TEMPLATE = "\uD83C\uDFB2 Релультат: %d";
    private final Provider<MessageService> messageServiceProvider;
    private final Random random;

    @Inject
    public RandomCommand(Provider<MessageService> messageServiceProvider) {
        this.messageServiceProvider = messageServiceProvider;
        this.random = new Random();
    }

    @Override
    public void execute(Update update) {
        LOGGER.info("execute:: start executing randomCommand");
        try {
            var msg = update.getMessage();
            var parts = msg.getText().split(" ");
            int min = 0, max;
            if (parts.length == 2) {
                max = Integer.parseInt(parts[1]);
            } else {
                min = Integer.parseInt(parts[1]);
                max = Integer.parseInt(parts[2]);
            }
            var response = String.format(TEMPLATE, random.nextInt(max - min + 1) + min);
            messageServiceProvider.get().replyOnMessage(msg.getChatId(), msg.getMessageId(), response, false);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            messageServiceProvider.get().replyOnMessage(update.getMessage().getChatId(), update.getMessage().getMessageId(), FAILED_COMMAND_MESSAGE, true);
        }
    }

    @Override
    public String getInvoker() {
        return "/r";
    }

}

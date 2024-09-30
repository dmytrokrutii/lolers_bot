package org.lolers.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.lolers.service.MessageService;
import org.lolers.service.PollService;
import org.lolers.storage.UserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;

@Singleton
public class TagCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagCommand.class.getName());

    private final Provider<MessageService> provider;
    private final PollService pollService;
    private final UserStorage userStorage;

    @Inject
    public TagCommand(Provider<MessageService> provider, PollService pollService, UserStorage userStorage) {
        this.provider = provider;
        this.pollService = pollService;
        this.userStorage = userStorage;
    }

    @Override
    public void execute(Update update) {
        LOGGER.info("execute:: start executing tagCommand");
        var messageService = provider.get();
        var chatId = update.getMessage().getChatId();
        try {
            messageService.sendMessage(update.getMessage().getChatId(), userStorage.getTagAllString(), false);
            var parts = update.getMessage().getText().split(" ");
            if (parts.length >= 2) {
                var args = Arrays.copyOfRange(parts, 1, parts.length);
                pollService.initPoll(chatId, String.join(" ", args));
            }
        } catch (Exception e) {
            messageService.replyOnMessage(chatId, update.getMessage().getMessageId(), FAILED_COMMAND_MESSAGE, true);
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public String getInvoker() {
        return "/all";
    }
}

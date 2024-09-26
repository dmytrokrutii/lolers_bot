package org.lolers.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.lolers.infrastructure.Mapper;
import org.lolers.service.MessageService;
import org.lolers.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

@Singleton
public class UnmuteCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnmuteCommand.class.getName());
    private static final String UNMUTE_MSG = "✅ %s розмутано, але наступного разу краще подумай перед тим як щось писати";
    private static final String USER_UNMUTED_MSG = "ℹ\uFE0F %s і так не в муті";

    private final Provider<MessageService> provider;

    @Inject
    public UnmuteCommand(Provider<MessageService> provider) {

        this.provider = provider;
    }

    @Override
    public void execute(Update update) {
        var tag = Mapper.getTag(update);
        var messageService = provider.get();
        var chatId = update.getMessage().getChatId();
        var messageId = update.getMessage().getMessageId();
        try {
            if (Storage.MutedUserStorage.isMuted(tag, chatId)) {
                Storage.MutedUserStorage.removeMutedUserByTag(tag);
                var msg = String.format(UNMUTE_MSG, tag);
                messageService.replyOnMessage(chatId, messageId, msg, false);
            } else {
                var msg = String.format(USER_UNMUTED_MSG, tag);
                messageService.replyOnMessage(chatId, messageId, msg, false);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            messageService.replyOnMessage(chatId, update.getMessage().getMessageId(), FAILED_COMMAND_MESSAGE, true);
        }
    }
}

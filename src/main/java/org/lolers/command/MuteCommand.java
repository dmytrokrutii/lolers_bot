package org.lolers.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.lolers.infrastructure.Mapper;
import org.lolers.service.MessageService;
import org.lolers.service.PollService;
import org.lolers.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

@Singleton
public class MuteCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(MuteCommand.class.getName());
    private final static String MAX_POLL_ERR_MSG = "⚠️ Одночасно можна створювати не більше %d голосувань";
    private final static String DURATION_ERR_MSG = "⚠️ Тривалість мута може бути в діапазоні від 1 до 60 хвилин";
    private final static String SELF_MUTE_MSG = "\uD83E\uDD21 як знаєш...";
    //language=HTML
    private final static String FAILED_MUTE_MESSAGE = """
            <b>❌ Викнила помилка при обробці команди</b>
            Приклад використання команди: <code>/mute @user time</code>
            Де <code>time</code> це ціле число в хвилинах від 1 до 60
            <i>FYI: Бота замутити не вийде, не намагайся</i>
            """;

    private final Provider<MessageService> provider;
    private final PollService pollService;

    @Inject
    public MuteCommand(PollService pollService, Provider<MessageService> provider) {
        this.pollService = pollService;
        this.provider = provider;
    }

    @Override
    public void execute(Update update) {
        LOGGER.info("execute:: start executing muteCommand");
        var messageService = provider.get();
        var chatId = update.getMessage().getChatId();
        try {
            if (Storage.PollStorage.isFull()) {
                messageService.sendMessage(chatId, String.format(MAX_POLL_ERR_MSG, Storage.PollStorage.MAX_SIZE), false);
                return;
            }
            var initDto = Mapper.toInitMutePollPayload(chatId, update);
            if (initDto.selfMute()) {
                messageService.replyOnMessage(chatId, update.getMessage().getMessageId(), SELF_MUTE_MSG, false);
            }
            if (initDto.duration() < 1 || initDto.duration() > 60) {
                messageService.replyOnMessage(chatId, update.getMessage().getMessageId(), DURATION_ERR_MSG, false);
                return;
            }
            pollService.initMutePoll(initDto);
        } catch (Exception e) {
            messageService.replyOnMessage(chatId, update.getMessage().getMessageId(), FAILED_MUTE_MESSAGE, true);
            LOGGER.error(e.getMessage());
        }
    }
}

package org.lolers.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.lolers.service.MessageService;
import org.lolers.service.RatingService;
import org.lolers.storage.UserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

@Singleton
public class MeCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(MeCommand.class);
    private static final String CLOWN = "\uD83E\uDD21";
    private static final String POWER = "\uD83D\uDCAA";

    private final Provider<MessageService> provider;
    private final RatingService ratingService;

    @Inject
    public MeCommand(Provider<MessageService> provider, RatingService ratingService, UserStorage userStorage) {
        this.provider = provider;
        this.ratingService = ratingService;
    }

    @Override
    public void execute(Update update) {
        LOGGER.info("execute:: start executing meCommand");
        try {
            var rating = ratingService.getRatingByUserId(update.getMessage().getFrom().getId());
            var msg = String.format("Ти маєш %d %s та %d %s", rating.clownCounter(), CLOWN, rating.powerCounter(), POWER);
            provider.get().replyOnMessage(update.getMessage().getChatId(), update.getMessage().getMessageId(), msg, false);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            provider.get().replyOnMessage(update.getMessage().getChatId(), update.getMessage().getMessageId(), FAILED_COMMAND_MESSAGE, true);
        }
    }

    @Override
    public String getInvoker() {
        return "/me";
    }
}

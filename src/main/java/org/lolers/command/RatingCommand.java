package org.lolers.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.lolers.service.MessageService;
import org.lolers.service.RatingService;
import org.lolers.storage.UserStorage;
import org.lolers.storage.entity.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.Comparator;

@Singleton
public class RatingCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(RatingCommand.class);
    private static final String CLOWN = "\uD83E\uDD21";
    private static final String POWER = "\uD83D\uDCAA";
    private static final String PRIZE = "🏆";
    private static final String LIKE = "\uD83D\uDC4D";
    private static final String FIRE = "\uD83D\uDD25";
    private static final String HEART = "❤";

    private final Provider<MessageService> provider;
    private final RatingService ratingService;
    private final UserStorage userStorage;

    @Inject
    public RatingCommand(Provider<MessageService> provider, RatingService ratingService, UserStorage userStorage) {
        this.provider = provider;
        this.ratingService = ratingService;
        this.userStorage = userStorage;
    }

    @Override
    public void execute(Update update) {
        LOGGER.info("execute:: start executing ratingsCommand");
        try {
            var ratings = new ArrayList<>(ratingService.getRatings());
            var msg = new StringBuilder();
            ratings.sort(Comparator.comparingInt(Rating::clownCounter).reversed());
            var topPowered = ratings.stream().max(Comparator.comparingInt(Rating::powerCounter)).orElse(null);
            var clownTop = String.format("<code>%sТоп клоун: %s</code>\n", PRIZE, userStorage.getUserById(ratings.get(0).id()).name());
            msg.append(clownTop);
            var powerTop = String.format("<code>%sНайпотужніший: %s</code>\n", PRIZE, userStorage.getUserById(topPowered.id()).name());
            msg.append(powerTop);
            msg.append("<b>Рейтингова таблиця:</b>\n");
            for (var rating : ratings) {
                msg.append(String.format("<i>%d %s та %d %s має %s</i>",
                                rating.clownCounter(),
                                CLOWN,
                                rating.powerCounter(),
                                POWER,
                                userStorage.getUserById(rating.id()).name()))
                        .append("\n");
            }
            msg.append(String.format("\n%s це потужність, яка складається з %s %s %s\n", POWER, LIKE, FIRE, HEART));
            provider.get().replyOnMessage(update.getMessage().getChatId(), update.getMessage().getMessageId(), msg.toString(), true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            provider.get().replyOnMessage(update.getMessage().getChatId(), update.getMessage().getMessageId(), FAILED_COMMAND_MESSAGE, true);
        }
    }

    @Override
    public String getInvoker() {
        return "/rating";
    }
}

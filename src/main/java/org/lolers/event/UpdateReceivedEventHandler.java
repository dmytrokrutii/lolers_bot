package org.lolers.event;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;
import org.lolers.command.CommandInvoker;
import org.lolers.dto.RatingPayload;
import org.lolers.service.CleanerService;
import org.lolers.service.RatingService;
import org.lolers.storage.MessageStorage;
import org.lolers.storage.MutedUserStorage;
import org.lolers.storage.PollStorage;
import org.lolers.storage.model.Votes;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Singleton
@SuppressWarnings("unused")
public class UpdateReceivedEventHandler {
    private final CommandInvoker commandInvoker;
    private final CleanerService cleanerService;
    private final PollStorage pollStorage;
    private final MutedUserStorage mutedUserStorage;
    private final MessageStorage messageStorage;
    private final RatingService ratingService;

    @Inject
    public UpdateReceivedEventHandler(CommandInvoker commandInvoker, CleanerService cleanerService, PollStorage pollStorage,
                                      MutedUserStorage mutedUserStorage, MessageStorage messageStorage, RatingService ratingService) {
        this.commandInvoker = commandInvoker;
        this.cleanerService = cleanerService;
        this.pollStorage = pollStorage;
        this.mutedUserStorage = mutedUserStorage;
        this.messageStorage = messageStorage;
        this.ratingService = ratingService;
    }

    @Handler(delivery = Invoke.Asynchronously)
    public void handle(UpdateReceivedEvent event) {
        var update = event.update();
        if (shouldBeCleaned(update)) {
            cleanerService.clean(update.getMessage());
            return;
        }
        if (update.hasMessage()) {
            var msg = update.getMessage();
            messageStorage.add(msg.getMessageId(), msg.getChatId(), msg.getFrom().getId());
            if (msg.hasText()) {
                Optional.of(msg.getText())
                        .filter(s -> s.startsWith("/"))
                        .map(txt -> txt.split(" "))
                        .map(arr -> arr[0])
                        .ifPresent(command -> commandInvoker.execute(command, update));
            }
        } else if (update.hasPoll()) {
            updatePollResults(update);
        } else if (update.getMessageReaction() != null) {
            var reaction = update.getMessageReaction();
            var payLoad = new RatingPayload(reaction.getMessageId(),
                    reaction.getChat().getId(),
                    reaction.getUser().getId(),
                    reaction.getNewReaction(),
                    reaction.getOldReaction());
            ratingService.updateRating(payLoad);
        }
    }

    private boolean shouldBeCleaned(Update update) {
        Long userId = null;
        if (update.hasMessage()) {
            userId = update.getMessage().getFrom().getId();
        } else if (update.hasCallbackQuery()) {
            userId = update.getCallbackQuery().getFrom().getId();
        } else if (update.hasInlineQuery()) {
            userId = update.getInlineQuery().getFrom().getId();
        } else if (update.hasChatMember()) {
            userId = update.getChatMember().getFrom().getId();
        }
        var chatId = Optional.ofNullable(update.getMessage())
                .map(Message::getChatId);
        return Optional.ofNullable(userId)
                .filter(it -> chatId.isPresent())
                .map(it -> mutedUserStorage.isMuted(it, chatId.get()))
                .orElse(false);
    }

    private void updatePollResults(Update update) {
        var poll = update.getPoll();
        var id = poll.getId();
        if (pollStorage.contains(id)) {
            var yesVotes = poll.getOptions().get(0).getVoterCount();
            var noVotes = poll.getOptions().get(1).getVoterCount();
            pollStorage.update(id, new Votes(yesVotes, noVotes));
        }
    }
}

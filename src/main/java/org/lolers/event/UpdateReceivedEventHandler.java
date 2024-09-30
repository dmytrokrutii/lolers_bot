package org.lolers.event;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;
import org.lolers.command.CommandInvoker;
import org.lolers.service.CleanerService;
import org.lolers.storage.MutedUserStorage;
import org.lolers.storage.PollStorage;
import org.lolers.storage.model.Votes;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Singleton
public class UpdateReceivedEventHandler {
    private final CommandInvoker commandInvoker;
    private final CleanerService cleanerService;
    private final PollStorage pollStorage;
    private final MutedUserStorage mutedUserStorage;

    @Inject
    public UpdateReceivedEventHandler(CommandInvoker commandInvoker, CleanerService cleanerService, PollStorage pollStorage, MutedUserStorage mutedUserStorage) {
        this.commandInvoker = commandInvoker;
        this.cleanerService = cleanerService;
        this.pollStorage = pollStorage;
        this.mutedUserStorage = mutedUserStorage;
    }

    @Handler(delivery = Invoke.Asynchronously)
    public void handle(UpdateReceivedEvent event) {
        var update = event.update();
        if (shouldBeCleaned(update)) {
            cleanerService.clean(update.getMessage());
            return;
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            Optional.of(update.getMessage().getText())
                    .filter(s -> s.startsWith("/"))
                    .map(txt -> txt.split(" "))
                    .map(arr -> arr[0])
                    .ifPresent(command -> commandInvoker.execute(command, update));
        } else if (update.hasPoll()) {
            updatePollResults(update);
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

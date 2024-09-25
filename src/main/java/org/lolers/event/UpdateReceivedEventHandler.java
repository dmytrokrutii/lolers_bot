package org.lolers.event;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;
import org.lolers.command.CommandInvoker;
import org.lolers.model.Votes;
import org.lolers.service.CleanerService;
import org.lolers.storage.Storage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Singleton
public class UpdateReceivedEventHandler {
    private final CommandInvoker commandInvoker;
    private final CleanerService cleanerService;

    @Inject
    public UpdateReceivedEventHandler(CommandInvoker commandInvoker, CleanerService cleanerService) {
        this.commandInvoker = commandInvoker;
        this.cleanerService = cleanerService;
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

    private static boolean shouldBeCleaned(Update update) {
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
        return Optional.ofNullable(userId)
                .map(Storage.MutedUserStorage::isMuted)
                .orElse(false);
    }

    private static void updatePollResults(Update update) {
        var poll = update.getPoll();
        var id = poll.getId();
        if (Storage.PollStorage.contains(id)) {
            var yesVotes = poll.getOptions().get(0).getVoterCount();
            var noVotes = poll.getOptions().get(1).getVoterCount();
            Storage.PollStorage.update(id, new Votes(yesVotes, noVotes));
        }
    }
}

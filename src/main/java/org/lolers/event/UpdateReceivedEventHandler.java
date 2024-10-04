package org.lolers.event;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;
import org.lolers.command.CommandInvoker;
import org.lolers.dto.RatingPayload;
import org.lolers.service.CleanerService;
import org.lolers.service.RatingService;
import org.lolers.service.TranscriptionService;
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
    private final TranscriptionService transcriptionService;

    @Inject
    public UpdateReceivedEventHandler(CommandInvoker commandInvoker, CleanerService cleanerService, PollStorage pollStorage,
                                      MutedUserStorage mutedUserStorage, MessageStorage messageStorage, RatingService ratingService,
                                      TranscriptionService transcriptionService) {
        this.commandInvoker = commandInvoker;
        this.cleanerService = cleanerService;
        this.pollStorage = pollStorage;
        this.mutedUserStorage = mutedUserStorage;
        this.messageStorage = messageStorage;
        this.ratingService = ratingService;
        this.transcriptionService = transcriptionService;
    }

    @Handler(delivery = Invoke.Asynchronously)
    public void handle(UpdateReceivedEvent event) {
        var update = event.update();
        if (shouldBeCleaned(update)) {
            cleanerService.clean(update.getMessage());
            return;
        }
        switch (update) {
            case Update u when u.hasMessage() && u.getMessage().hasVoice() -> handleVoiceMessage(u);
            case Update u when u.hasMessage() -> handleMessage(u);
            case Update u when u.hasPoll() -> handlePollResults(u);
            case Update u when u.getMessageReaction() != null -> handleReaction(u);
            default -> {
            }
        }
    }

    private void handleMessage(Update update) {
        var msg = update.getMessage();
        updateRatingOnReply(msg);
        messageStorage.add(msg.getMessageId(), msg.getChatId(), msg.getFrom().getId());
        if (msg.hasText()) {
            Optional.of(msg.getText())
                    .filter(s -> s.startsWith("/"))
                    .map(txt -> txt.split(" "))
                    .map(arr -> arr[0])
                    .ifPresent(command -> commandInvoker.execute(command, update));
        }
    }

    private void handleReaction(Update update) {
        var reaction = update.getMessageReaction();
        var payLoad = new RatingPayload(reaction.getMessageId(),
                reaction.getChat().getId(),
                reaction.getUser().getId(),
                reaction.getNewReaction(),
                reaction.getOldReaction());
        ratingService.updateRating(payLoad);
    }

    private void handleVoiceMessage(Update update) {
        var field = update.getMessage().getVoice().getFileId();
        var msg = update.getMessage();
        transcriptionService.transcribe(field, msg.getChatId(), msg.getMessageId());
    }

    private Long getUserId(Update update) {
        return switch (update) {
            case Update u when u.hasMessage() -> u.getMessage().getFrom().getId();
            case Update u when u.hasCallbackQuery() -> u.getCallbackQuery().getFrom().getId();
            case Update u when u.hasInlineQuery() -> u.getInlineQuery().getFrom().getId();
            case Update u when u.hasChatMember() -> u.getChatMember().getFrom().getId();
            default -> null;
        };
    }

    private boolean shouldBeCleaned(Update update) {
        var userId = getUserId(update);
        var chatId = Optional.ofNullable(update.getMessage())
                .map(Message::getChatId);
        return Optional.ofNullable(userId)
                .filter(it -> chatId.isPresent())
                .map(it -> mutedUserStorage.isMuted(it, chatId.get()))
                .orElse(false);
    }

    private void handlePollResults(Update update) {
        var poll = update.getPoll();
        var id = poll.getId();
        if (pollStorage.contains(id)) {
            var yesVotes = poll.getOptions().get(0).getVoterCount();
            var noVotes = poll.getOptions().get(1).getVoterCount();
            pollStorage.update(id, new Votes(yesVotes, noVotes));
        }
    }

    private void updateRatingOnReply(Message message) {
        var userId = message.getFrom().getId();
        var replyTo = message.getReplyToMessage();
        String payload = null;
        Long replyUserId = null;
        if (replyTo != null) {
            replyUserId = replyTo.getFrom().getId();
            if (message.hasText()) {
                payload = message.getText();
            } else if (message.hasSticker()) {
                payload = message.getSticker().getEmoji();
            }
        }
        if (payload != null && !userId.equals(replyUserId)) {
            ratingService.updateRating(replyUserId, payload);
        }
    }
}

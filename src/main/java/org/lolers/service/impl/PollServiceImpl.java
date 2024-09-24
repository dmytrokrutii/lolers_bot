package org.lolers.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.lolers.command.Command;
import org.lolers.infrastructure.Mapper;
import org.lolers.infrastructure.schedule.SchedulerService;
import org.lolers.model.InitMutePollPayload;
import org.lolers.service.MessageService;
import org.lolers.service.MuteService;
import org.lolers.service.PollService;
import org.lolers.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class PollServiceImpl implements PollService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PollServiceImpl.class.getName());
    private static final int POLL_DURATION = 60;
    private static final String MUTE_QUESTION = "\uD83D\uDDF3 Мутаємо %s на %d хвилин(у)?\nℹ\uFE0F Голосування закриється після %d секунд";
    private static final String BASE_POLL_QUESTION = "\uD83D\uDDF3 %s?";
    private static final List<String> MUTE_OPTIONS = List.of("✅ В мут", "❌ Не треба");
    private static final List<String> BASE_POLL_OPTIONS = List.of("✅ Підтримую", "❌ Не підтримую", "❓ Ще не визначився");

    private final MessageService messageService;
    private final MuteService muteService;


    @Inject
    public PollServiceImpl(MessageService messageService, MuteService muteService) {
        this.messageService = messageService;
        this.muteService = muteService;
    }

    @Override
    public void initMutePoll(InitMutePollPayload payload) {
        LOGGER.info("initMutePoll:: Creating mute poll");
        try {
            var poll = Mapper.toMutePoll(payload, MUTE_QUESTION, MUTE_OPTIONS, POLL_DURATION);
            var msg = messageService.sendMessage(poll);
            var pollId = msg.getPoll().getId();
            Storage.MutedUserStorage.addMutedCandidate(pollId, payload.messageId(), payload.user(), payload.duration());
            SchedulerService.scheduleTask(() -> muteService.mute(pollId, payload.chatId()), POLL_DURATION + 10, TimeUnit.SECONDS);
            Storage.PollStorage.add(pollId);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            messageService.replyOnMessage(payload.chatId(), payload.messageId(), Command.FAILED_COMMAND_MESSAGE, true);
        }
    }

    @Override
    public void initGamePoll(long chatId, String question) {
        var poll = Mapper.toPoll(chatId, question, BASE_POLL_QUESTION, BASE_POLL_OPTIONS);
        try {
            messageService.sendMessage(poll);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            messageService.sendMessage(chatId, Command.FAILED_COMMAND_MESSAGE, true);
        }

    }
}

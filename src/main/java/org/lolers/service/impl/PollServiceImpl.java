package org.lolers.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.lolers.command.Command;
import org.lolers.dto.InitMutePollPayload;
import org.lolers.infrastructure.Mapper;
import org.lolers.infrastructure.schedule.SchedulerService;
import org.lolers.service.MessageService;
import org.lolers.service.MuteService;
import org.lolers.service.PollService;
import org.lolers.storage.MutedUserStorage;
import org.lolers.storage.PollStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class PollServiceImpl implements PollService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PollServiceImpl.class.getName());
    private static final int POLL_DURATION = 60;
    private static final String MUTE_QUESTION = "\uD83D\uDDF3 Мутаємо %s на %d хвилин(у)?\nℹ️ Голосування закриється після %d секунд";
    private static final String BASE_POLL_QUESTION = "\uD83D\uDDF3 %s?";
    private static final List<String> MUTE_OPTIONS = List.of("✅ В мут", "❌ Не треба");
    private static final List<String> BASE_POLL_OPTIONS = List.of("✅ Підтримую", "❌ Не підтримую", "❓ Ще не визначився");

    private final MessageService messageService;
    private final MuteService muteService;
    private final SchedulerService schedulerService;
    private final MutedUserStorage mutedUserStorage;
    private final PollStorage pollStorage;
    private final Mapper mapper;

    @Inject
    public PollServiceImpl(MessageService messageService, MuteService muteService, Mapper mapper, MutedUserStorage mutedUserStorage, PollStorage pollStorage, SchedulerService schedulerService) {
        this.messageService = messageService;
        this.muteService = muteService;
        this.mapper = mapper;
        this.mutedUserStorage = mutedUserStorage;
        this.pollStorage = pollStorage;
        this.schedulerService = schedulerService;
    }

    @Override
    public void initMutePoll(InitMutePollPayload payload) {
        LOGGER.info("initMutePoll:: Creating mute poll");
        try {
            var poll = mapper.toMutePoll(payload, MUTE_QUESTION, MUTE_OPTIONS, POLL_DURATION);
            var msg = messageService.sendMessage(poll);
            var pollId = msg.getPoll().getId();
            mutedUserStorage.addMutedCandidate(pollId, payload.messageId(), payload.user(), payload.duration(), payload.chatId());
            schedulerService.scheduleTask(() -> muteService.mute(pollId, payload.chatId()), POLL_DURATION + 5, TimeUnit.SECONDS);
            pollStorage.add(pollId);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            messageService.replyOnMessage(payload.chatId(), payload.messageId(), Command.FAILED_COMMAND_MESSAGE, true);
        }
    }

    @Override
    public void initPoll(long chatId, String question) {
        var poll = mapper.toPoll(chatId, question, BASE_POLL_QUESTION, BASE_POLL_OPTIONS);
        try {
            messageService.sendMessage(poll);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            messageService.sendMessage(chatId, Command.FAILED_COMMAND_MESSAGE, true);
        }
    }
}

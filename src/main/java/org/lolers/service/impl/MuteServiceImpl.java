package org.lolers.service.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.lolers.command.Command;
import org.lolers.infrastructure.schedule.SchedulerService;
import org.lolers.service.MessageService;
import org.lolers.service.MuteService;
import org.lolers.storage.MutedUserStorage;
import org.lolers.storage.PollStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@Singleton
public class MuteServiceImpl implements MuteService {
    private final static Logger LOGGER = LoggerFactory.getLogger(MuteServiceImpl.class.getName());
    private static final String UNMUTE_MSG = "✅ %s час мута сплинув, можеш писати повідомлення";
    //language=HTML
    private final static String TEMPLATE = """
            <b>📊 Голосування завершено!</b>
            ⚖️ Більшість вирішила:
            %s
            """;
    //language=HTML
    private final static String MUTE_MESSAGE = """
            <i>✅ Підтримати голосування і відправити клоуна в мут. %s, наступного разу слідкуй за базаром.</i>
            """;
    //language=HTML
    private final static String FAILED_MUTE_MESSAGE = """
            <i>❌ %s не заслужив такої участі. Можливо це тебе потрібно додати в мут?</i>
            """;

    private final Provider<MessageService> messageService;
    private final SchedulerService schedulerService;
    private final MutedUserStorage mutedUserStorage;
    private final PollStorage pollStorage;

    @Inject
    MuteServiceImpl(Provider<MessageService> messageServiceProvider, MutedUserStorage mutedUserStorage, PollStorage pollStorage, SchedulerService schedulerService) {
        this.messageService = messageServiceProvider;
        this.mutedUserStorage = mutedUserStorage;
        this.pollStorage = pollStorage;
        this.schedulerService = schedulerService;
    }

    @Override
    public void mute(String pollId, long chatId) {
        LOGGER.info("mute:: Starting mute process");
        var votes = pollStorage.get(pollId);
        var mutedUser = mutedUserStorage.getMutedUser(pollId);
        var messageId = mutedUser.messageId();
        try {
            var tag = mutedUser.user().tag();
            if (votes.yes() <= votes.no() || votes.yes() <= 1) {
                mutedUserStorage.removeMutedUserByPoll(pollId);
                pollStorage.remove(pollId);
                var msg = String.format(TEMPLATE, String.format(FAILED_MUTE_MESSAGE, tag));
                messageService.get().replyOnMessage(chatId, messageId, msg, true);
                return;
            }
            var muteEndTime = System.currentTimeMillis() + getDurationInMillis(mutedUser.muteDurationMinutes());
            mutedUserStorage.setMuted(pollId, muteEndTime);
            pollStorage.remove(pollId);
            schedulerService.scheduleTask(() -> {
                if (mutedUserStorage.isMutedByPollId(pollId)) {
                    messageService.get().sendMessage(chatId, String.format(UNMUTE_MSG, tag), false);
                    mutedUserStorage.removeMutedUserByPoll(pollId);
                }
            }, mutedUser.muteDurationMinutes(), TimeUnit.MINUTES);
            var msg = String.format(TEMPLATE, String.format(MUTE_MESSAGE, tag));
            messageService.get().replyOnMessage(chatId, messageId, msg, true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            messageService.get().replyOnMessage(chatId, messageId, Command.FAILED_COMMAND_MESSAGE, true);
        }
    }

    private long getDurationInMillis(long duration) {
        return duration * 60 * 1000;
    }
}

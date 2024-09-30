package org.lolers.infrastructure;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.lolers.dto.InitMutePollPayload;
import org.lolers.storage.UserStorage;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Singleton
public class Mapper {
    private final UserStorage userStorage;

    @Inject
    public Mapper(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public InitMutePollPayload toInitMutePollPayload(long chatId, Update update) {
        var parts = update.getMessage().getText().split(" ");
        var tag = parts[1];
        var user = userStorage.getUser(tag);
        var selfMute = false;
        if (user.id() == update.getMessage().getFrom().getId()) {
            selfMute = true;
        }
        var duration = Integer.parseInt(parts[2]);
        var messageId = update.getMessage().getMessageId();
        return new InitMutePollPayload(chatId, messageId, user, duration, selfMute);
    }

    public SendPoll toMutePoll(InitMutePollPayload dto, String question, List<String> options, int pollDuration) {
        var poll = new SendPoll();
        poll.setChatId(dto.chatId());
        poll.setQuestion(String.format(question, dto.user().accusativeName(), dto.duration(), pollDuration));
        poll.setOptions(options);
        poll.setAllowMultipleAnswers(false);
        poll.setIsAnonymous(false);
        poll.setOpenPeriod(pollDuration);
        return poll;
    }

    public SendPoll toPoll(long chatId, String question, String template, List<String> options) {
        var poll = new SendPoll();
        poll.setChatId(chatId);
        poll.setQuestion(String.format(template, question));
        poll.setOptions(options);
        poll.setAllowMultipleAnswers(true);
        poll.setIsAnonymous(false);
        return poll;
    }

    public String getTag(Update update) {
        var parts = update.getMessage().getText().split(" ");
        return parts[1];
    }
}

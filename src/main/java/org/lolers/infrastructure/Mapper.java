package org.lolers.infrastructure;

import org.lolers.model.InitMutePollPayload;
import org.lolers.storage.Storage;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class Mapper {

    public static InitMutePollPayload toInitMutePollPayload(long chatId, Update update) {
        var parts = update.getMessage().getText().split(" ");
        var tag = parts[1];
        var user = Storage.UserStorage.getUser(tag);
        var selfMute = false;
        if (user.id() == update.getMessage().getFrom().getId()) {
            selfMute = true;
        }
        var duration = Integer.parseInt(parts[2]);
        var messageId = update.getMessage().getMessageId();
        return new InitMutePollPayload(chatId, messageId, user, duration, selfMute);
    }

    public static SendPoll toMutePoll(InitMutePollPayload dto, String question, List<String> options, int pollDuration) {
        var poll = new SendPoll();
        poll.setChatId(dto.chatId());
        poll.setQuestion(String.format(question, dto.user().accusativeName(), dto.duration(), pollDuration));
        poll.setOptions(options);
        poll.setAllowMultipleAnswers(false);
        poll.setIsAnonymous(false);
        poll.setOpenPeriod(pollDuration);
        return poll;
    }

    public static SendPoll toPoll(long chatId, String question, String template, List<String> options) {
        var poll = new SendPoll();
        poll.setChatId(chatId);
        poll.setQuestion(String.format(template, question));
        poll.setOptions(options);
        poll.setAllowMultipleAnswers(true);
        poll.setIsAnonymous(false);
        return poll;
    }

    public static String getTag(Update update) {
        var parts = update.getMessage().getText().split(" ");
        return parts[1];
    }
}

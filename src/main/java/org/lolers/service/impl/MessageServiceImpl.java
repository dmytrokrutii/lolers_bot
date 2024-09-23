package org.lolers.service.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.lolers.infrastructure.Bot;
import org.lolers.service.MessageService;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.inject.Singleton;

@Singleton
public class MessageServiceImpl implements MessageService {
    private final Provider<Bot> botProvider;

    @Inject
    public MessageServiceImpl(Provider<Bot> botProvider) {
        this.botProvider = botProvider;
    }

    @Override
    public Message sendMessage(BotApiMethodMessage poll) {
        try {
            return botProvider.get().execute(poll);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendMessage(long chatId, String text, boolean isHtml) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        if (isHtml) {
            sendMessage.setParseMode(ParseMode.HTML);
        }
        try {
            botProvider.get().execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void replyOnMessage(long chatId, int messageId, String text, boolean isHtml) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyToMessageId(messageId);
        if (isHtml) {
            sendMessage.setParseMode(ParseMode.HTML);
        }
        try {
            botProvider.get().execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteMessage(DeleteMessage message) {
        try {
            botProvider.get().execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}

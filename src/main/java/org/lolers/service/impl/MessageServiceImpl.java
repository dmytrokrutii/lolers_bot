package org.lolers.service.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.lolers.infrastructure.Bot;
import org.lolers.service.MessageService;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Singleton
public class MessageServiceImpl implements MessageService {
    private final Provider<Bot> botProvider;
    private static final String FILE_URL = "https://api.telegram.org/file/bot%s/%s";
    private final String TOKEN;

    @Inject
    public MessageServiceImpl(Provider<Bot> botProvider, @Named("bot.token") String token) {
        this.botProvider = botProvider;
        this.TOKEN = token;
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

    @Override
    public String getFileUrl(String fileId) {
        try {
            var getFile = new GetFile();
            getFile.setFileId(fileId);
            var file = botProvider.get().execute(getFile);
            return String.format(FILE_URL, TOKEN, file.getFilePath());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}

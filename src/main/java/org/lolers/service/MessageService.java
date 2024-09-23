package org.lolers.service;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageService {

    Message sendMessage(BotApiMethodMessage msg);

    void sendMessage(long chatId, String text, boolean isHtml);

    void replyOnMessage(long chatId, int messageId, String text, boolean isHtml);

    void deleteMessage(DeleteMessage msg);
}

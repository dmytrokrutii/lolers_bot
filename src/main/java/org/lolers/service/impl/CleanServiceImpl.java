package org.lolers.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.lolers.service.CleanerService;
import org.lolers.service.MessageService;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.logging.Logger;

@Singleton
public class CleanServiceImpl implements CleanerService {
    private static final Logger LOGGER = Logger.getLogger(CleanServiceImpl.class.getName());

    private final MessageService messageService;

    @Inject
    public CleanServiceImpl(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void clean(Message message) {
        LOGGER.info(String.format("Deleting message from %d", message.getFrom().getId()));
        var deleteMsg = new DeleteMessage();
        deleteMsg.setChatId(message.getChatId());
        deleteMsg.setMessageId(message.getMessageId());
        try {
            messageService.deleteMessage(deleteMsg);
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }
}

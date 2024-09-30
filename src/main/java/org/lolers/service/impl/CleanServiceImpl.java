package org.lolers.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.lolers.service.CleanerService;
import org.lolers.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Singleton
public class CleanServiceImpl implements CleanerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanServiceImpl.class.getName());

    private final MessageService messageService;

    @Inject
    public CleanServiceImpl(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void clean(Message message) {
        LOGGER.info("Deleting message from {}", message.getFrom().getId());
        var deleteMsg = new DeleteMessage();
        deleteMsg.setChatId(message.getChatId());
        deleteMsg.setMessageId(message.getMessageId());
        try {
            messageService.deleteMessage(deleteMsg);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}

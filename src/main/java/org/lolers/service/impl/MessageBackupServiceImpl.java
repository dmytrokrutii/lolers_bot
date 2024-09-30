package org.lolers.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.sauronsoftware.cron4j.Scheduler;
import org.lolers.service.MessageBackupService;
import org.lolers.storage.MessageStorage;
import org.lolers.storage.entity.Message;
import org.lolers.storage.repository.SupabaseClient;
import org.lolers.storage.repository.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MessageBackupServiceImpl implements MessageBackupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBackupServiceImpl.class);
    private static final String CRON = "0 0 * * *";
    private final MessageStorage messageStorage;
    private final SupabaseClient<Message> supabaseClient;
    private final Scheduler scheduler;

    @Inject
    public MessageBackupServiceImpl(MessageStorage messageStorage, SupabaseClient<Message> supabaseClient, Scheduler scheduler) {
        this.messageStorage = messageStorage;
        this.supabaseClient = supabaseClient;
        this.scheduler = scheduler;
    }

    @Override
    public void start() {
        scheduler.schedule(CRON, this::backupData);
        scheduler.start();
        LOGGER.info("Backup messages job scheduled to run every 24 hours.");
    }

    @Override
    public void backupData() {
        LOGGER.info("Starting backup...");
        var messages = messageStorage.getAll();
        if (messages.isEmpty()) {
            LOGGER.info("No messages to back up.");
            return;
        }
        var records = messages.entrySet().stream()
                .map(entry -> new Message(entry.getKey().component1(), entry.getKey().component2(), entry.getValue()))
                .toList();
        try {
            supabaseClient.bulkSave(Table.MESSAGES, records);
            LOGGER.info("Backup completed successfully. {} records saved.", records.size());
            messageStorage.clear();
            LOGGER.info("Cleared stored messages from memory.");
        } catch (Exception e) {
            LOGGER.error("Failed to back up messages to Supabase", e);
        }
    }
}

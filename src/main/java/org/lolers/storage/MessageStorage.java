package org.lolers.storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import kotlin.Pair;
import org.lolers.storage.entity.Message;
import org.lolers.storage.repository.SupabaseClient;
import org.lolers.storage.repository.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class MessageStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageStorage.class);
    private static final String ID = "id";
    private static final String CHAT_ID = "chatId";

    private final SupabaseClient<Message> messageSupabaseClient;
    private final Map<Pair<Integer, Long>, Long> messages = new ConcurrentHashMap<>();

    @Inject
    public MessageStorage(SupabaseClient<Message> messageSupabaseClient) {
        this.messageSupabaseClient = messageSupabaseClient;
    }

    public void add(int messageId, long chatId, long userId) {
        messages.put(new Pair<>(messageId, chatId), userId);
    }

    public Long getUserIdByMessageId(int messageId, long chatId) {
        LOGGER.info("getUserIdByMessageId:: get in memory message from chatId: {} by id: {}", messageId, chatId);
        var userId = messages.get(new Pair<>(messageId, chatId));
        if (userId == null) {
            try {
                LOGGER.info("getUserIdByMessageId:: get stored message from chatId: {} by id: {}", messageId, chatId);
                var filter = Map.of(ID, String.valueOf(messageId), CHAT_ID, String.valueOf(chatId));
                userId = messageSupabaseClient
                        .getBy(Table.MESSAGES, filter, Message[].class)
                        .userId();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        return userId;
    }

    public Map<Pair<Integer, Long>, Long> getAll() {
        return new HashMap<>(messages);
    }

    public void clear() {
        messages.clear();
    }
}

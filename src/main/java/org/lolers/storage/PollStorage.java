package org.lolers.storage;

import com.google.inject.Singleton;
import org.lolers.storage.model.Votes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class PollStorage {
    private final int MAX_SIZE = 3;

    private final Map<String, Votes> polls = new ConcurrentHashMap<>();

    public void add(String id) {
        if (!isFull()) {
            polls.put(id, new Votes(0, 0));
        }
    }

    public Votes get(String id) {
        return polls.get(id);
    }

    public void remove(String id) {
        polls.remove(id);
    }

    public boolean contains(String id) {
        return polls.containsKey(id);
    }

    public void update(String id, Votes votes) {
        polls.computeIfPresent(id, (k, v) -> votes);
    }

    public boolean isFull() {
        return polls.size() >= MAX_SIZE;
    }

    public int getMaxSize() {
        return MAX_SIZE;
    }
}

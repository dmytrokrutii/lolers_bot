package org.lolers.storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.lolers.storage.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class UserStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserStorage.class);

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final String TAG_ALL;

    @Inject
    public UserStorage(List<User> users) {
        users.forEach(user -> this.users.put(user.tag(), user));
        this.TAG_ALL = users.stream()
                .map(User::tag)
                .collect(Collectors.joining(" "));
    }

    public User getUser(String tag) {
        return users.get(tag);
    }

    public User getUserById(long id) {
        return users.values().stream()
                .filter(user -> user.id() == id)
                .findFirst()
                .orElse(null);
    }

    public String getTagAllString() {
        return TAG_ALL;
    }
}

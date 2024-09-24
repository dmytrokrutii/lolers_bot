package org.lolers.storage;

import org.lolers.model.MutedUserModel;
import org.lolers.model.User;
import org.lolers.model.Votes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Storage {
    private static final Logger LOGGER = LoggerFactory.getLogger(Storage.class.getName());
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static class UserStorage {
        private static final Map<String, User> users = new ConcurrentHashMap<>();
        private static String TAG_ALL;

        static {
            loadUsers();
        }

        private static void loadUsers() {
            List<User> data;
            try {
                data = List.of(
                        new User(293720346L, "Чорний", "Чорного", "@solosuicide", dateFormat.parse("2000-04-21")),
                        new User(280777567L, "Білий", "Білого", "@dmitry4444", dateFormat.parse("2000-08-21")),
                        new User(613997008L, "Кек", "Кека", "@DimaLelikov", dateFormat.parse("2004-12-23")),
                        new User(131843596L, "Костя", "Костю", "@lelikov_k", dateFormat.parse("2000-08-28")),
                        new User(841782402L, "Мірон", "Мірона", "@muronyukk", dateFormat.parse("2000-09-11")),
                        new User(443889933L, "Макс", "Макса", "@sparkwf", dateFormat.parse("1999-11-29")),
                        new User(279939489L, "Нікіта", "Нікіту", "@sweetsquid", dateFormat.parse("2000-11-24")),
                        new User(386512919L, "Іван", "Івана", "@kkromka", dateFormat.parse("2000-03-25"))
                );
                TAG_ALL = data.stream()
                        .map(User::tag)
                        .collect(Collectors.joining(" "));
            } catch (ParseException e) {
                LOGGER.error("loadUsers:: failed to load users data");
                throw new RuntimeException(e);
            }
            data.forEach(user -> users.put(user.tag(), user));
        }

        public static User getUser(String tag) {
            return users.get(tag);
        }

        public static String getTagAllString() {
            return TAG_ALL;
        }
    }

    public static class MutedUserStorage {
        private static final Map<String, MutedUserModel> mutedUsers = new ConcurrentHashMap<>();

        public static void addMutedCandidate(String pollId, int messageId, User user, int duration) {
            mutedUsers.put(pollId, new MutedUserModel(user, messageId, duration, 0, false));
        }

        public static MutedUserModel getMutedUser(String pollId) {
            return mutedUsers.get(pollId);
        }

        public static void setMuted(String pollId, long endTime) {
            mutedUsers.computeIfPresent(pollId, (key, mutedUserModel) ->
                    new MutedUserModel(
                            mutedUserModel.user(),
                            mutedUserModel.messageId(),
                            mutedUserModel.muteDurationMinutes(),
                            endTime,
                            true
                    )
            );
        }

        public static boolean isMuted(long userId) {
            return mutedUsers.values()
                    .stream()
                    .filter(u -> u.user().id() == userId)
                    .findFirst()
                    .map(MutedUserModel::muted)
                    .orElse(false);
        }

        public static void removeMutedUserByPoll(String pollId) {
            mutedUsers.remove(pollId);
        }

        public static void removeMutedUserByTag(String tag) {
            mutedUsers.values()
                    .stream()
                    .filter(mutedUser -> mutedUser.user().tag().equals(tag))
                    .findFirst()
                    .ifPresent(mutedUserToRemove -> mutedUsers.entrySet().removeIf(entry -> entry.getValue().equals(mutedUserToRemove)));

        }

        public static Boolean isMuted(String tag) {
            return mutedUsers.values()
                    .stream()
                    .filter(u -> Objects.equals(u.user().tag(), tag))
                    .findFirst()
                    .map(MutedUserModel::muted)
                    .orElse(false);
        }
    }

    public static class PollStorage {
        public static final int MAX_SIZE = 3;

        private static final Map<String, Votes> polls = new ConcurrentHashMap<>();

        public static void add(String id) {
            if (!isFull()) {
                polls.put(id, new Votes(0, 0));
            }
        }

        public static Votes get(String id) {
            return polls.get(id);
        }

        public static void remove(String id) {
            polls.remove(id);
        }

        public static boolean contains(String id) {
            return polls.containsKey(id);
        }

        public static void update(String id, Votes votes) {
            polls.computeIfPresent(id, (k, v) -> votes);
        }

        public static boolean isFull() {
            return polls.size() >= MAX_SIZE;
        }
    }

}

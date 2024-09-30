package org.lolers.storage;

import com.google.inject.Singleton;
import org.lolers.storage.entity.User;
import org.lolers.storage.model.MutedUser;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class MutedUserStorage {
    private final Map<String, MutedUser> mutedUsers = new ConcurrentHashMap<>();

    public void addMutedCandidate(String pollId, int messageId, User user, int duration, long chatId) {
        mutedUsers.put(pollId, new MutedUser(user, messageId, chatId, duration, 0, false));
    }

    public MutedUser getMutedUser(String pollId) {
        return mutedUsers.get(pollId);
    }

    public void setMuted(String pollId, long endTime) {
        mutedUsers.computeIfPresent(pollId, (key, mutedUser) ->
                new MutedUser(
                        mutedUser.user(),
                        mutedUser.messageId(),
                        mutedUser.chatId(),
                        mutedUser.muteDurationMinutes(),
                        endTime,
                        true
                )
        );
    }

    public boolean isMuted(long userId, long chatId) {
        return mutedUsers.values()
                .stream()
                .filter(u -> u.user().id() == userId && u.chatId() == chatId)
                .findFirst()
                .map(MutedUser::muted)
                .orElse(false);
    }

    public void removeMutedUserByPoll(String pollId) {
        mutedUsers.remove(pollId);
    }

    public void removeMutedUserByTag(String tag) {
        mutedUsers.values()
                .stream()
                .filter(mutedUser -> mutedUser.user().tag().equals(tag))
                .findFirst()
                .ifPresent(mutedUserToRemove -> mutedUsers.entrySet().removeIf(entry -> entry.getValue().equals(mutedUserToRemove)));

    }

    public boolean isMuted(String tag, long chatId) {
        return mutedUsers.values()
                .stream()
                .filter(u -> Objects.equals(u.user().tag(), tag) && u.chatId() == chatId)
                .findFirst()
                .map(MutedUser::muted)
                .orElse(false);
    }

    public boolean isMutedByPollId(String pollId) {
        return Optional.ofNullable(mutedUsers.get(pollId))
                .map(MutedUser::muted)
                .orElse(false);
    }
}

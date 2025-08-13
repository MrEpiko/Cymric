package me.mrepiko.cymric.discord;

import me.mrepiko.cymric.DiscordBot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DiscordCache {

    @NotNull
    private static final Map<String, User> cachedUsers = new HashMap<>();
    @NotNull
    private static final Map<String, Message> cachedMessages = new HashMap<>();

    @NotNull
    public static User getUserById(@NotNull String id) {
        return getUserById(id, false);
    }

    @NotNull
    public static User getUserById(@NotNull String id, boolean force) {
        if (force) {
            User user = getUserRestAction(id).complete();
            cachedUsers.put(id, user);
            return user;
        }

        if (cachedUsers.containsKey(id)) {
            return cachedUsers.get(id);
        }
        User user = getUserRestAction(id).complete();
        cachedUsers.put(id, user);

        return user;
    }

    public static void getUserById(@NotNull String id, @NotNull Consumer<User> consumer) {
        getUserById(id, false, consumer);
    }

    public static void getUserById(@NotNull String id, boolean force, @NotNull Consumer<User> consumer) {
        if (force) {
            getUserRestAction(id).queue(user -> {
                cachedUsers.put(id, user);
                consumer.accept(user);
            }, throwable -> consumer.accept(null));
        }

        if (cachedUsers.containsKey(id)) {
            consumer.accept(cachedUsers.get(id));
            return;
        }
        getUserRestAction(id).queue(user -> {
            cachedUsers.put(id, user);
            consumer.accept(user);
        }, throwable -> consumer.accept(null));
    }

    @NotNull
    public static Message getMessageById(@NotNull String messageId, @NotNull MessageChannel messageChannel) {
        return getMessageById(messageId, messageChannel, false);
    }

    @NotNull
    public static Message getMessageById(@NotNull String messageId, @NotNull MessageChannel messageChannel, boolean force) {
        if (force) {
            Message message = messageChannel.retrieveMessageById(messageId).complete();
            cachedMessages.put(messageId, message);
            return message;
        }

        if (cachedMessages.containsKey(messageId)) {
            return cachedMessages.get(messageId);
        }
        Message message = messageChannel.retrieveMessageById(messageId).complete();
        cachedMessages.put(messageId, message);

        return message;
    }

    public static void getMessageById(@NotNull String messageId, @NotNull MessageChannel messageChannel, @NotNull Consumer<Message> consumer) {
        getMessageById(messageId, messageChannel, false, consumer);
    }

    public static void getMessageById(@NotNull String messageId, @NotNull MessageChannel messageChannel, boolean force, @NotNull Consumer<Message> consumer) {
        if (force) {
            messageChannel.retrieveMessageById(messageId).queue(message -> {
                cachedMessages.put(messageId, message);
                consumer.accept(message);
            }, throwable -> consumer.accept(null));
        }

        if (cachedMessages.containsKey(messageId)) {
            consumer.accept(cachedMessages.get(messageId));
            return;
        }
        messageChannel.retrieveMessageById(messageId).queue(message -> {
            cachedMessages.put(messageId, message);
            consumer.accept(message);
        }, throwable -> consumer.accept(null));
    }

    public static void cacheMessage(@NotNull Message message) {
        cachedMessages.put(message.getId(), message);
    }

    private static RestAction<User> getUserRestAction(String id) {
        return DiscordBot.getInstance().getShardManager().retrieveUserById(id);
    }

}

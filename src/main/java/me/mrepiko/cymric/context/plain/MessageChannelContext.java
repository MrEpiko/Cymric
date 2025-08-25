package me.mrepiko.cymric.context.plain;

import me.mrepiko.cymric.context.Context;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.placeholders.PlaceholderMapBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.NotNull;

public interface MessageChannelContext extends Context {

    @NotNull
    MessageChannel getMessageChannel();

    @NotNull
    default GuildMessageChannel getGuildMessageChannel() {
        MessageChannel messageChannel = getMessageChannel();
        if (messageChannel instanceof GuildMessageChannel guildMessageChannel) {
            return guildMessageChannel;
        } else {
            throw new IllegalStateException("This context does not have a GuildMessageChannel.");
        }
    }

    @NotNull
    default PrivateChannel getPrivateChannel() {
        MessageChannel messageChannel = getMessageChannel();
        if (messageChannel instanceof PrivateChannel privateChannel) {
            return privateChannel;
        } else {
            throw new IllegalStateException("This context does not have a PrivateChannel.");
        }
    }

    @NotNull
    default PlaceholderMap getPlaceholderMap() {
        return getPlaceholderMap(false);
    }

    @NotNull
    default PlaceholderMap getPlaceholderMap(boolean includeCommandPlaceholders) {
        return PlaceholderMapBuilder.create(this)
                .includeCommandPlaceholders(includeCommandPlaceholders)
                .build();
    }

}

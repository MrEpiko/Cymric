package me.mrepiko.cymric.context.plain.impl;

import lombok.AllArgsConstructor;
import me.mrepiko.cymric.context.plain.MessageContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public class MessageContextImpl implements MessageContext {

    private final Message message;

    @Override
    @NotNull
    public Message getMessage() {
        return message;
    }

    @NotNull
    @Override
    public MessageChannel getMessageChannel() {
        return message.getChannel();
    }

    @Override
    @NotNull
    public User getUser() {
        return message.getAuthor();
    }

    @Nullable
    @Override
    public Guild getGuild() {
        try {
            return (message.isFromGuild()) ? message.getGuild() : null;
        } catch (IllegalStateException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public Member getMember() {
        Guild guild = getGuild();
        return (guild != null) ? guild.retrieveMember(getUser()).complete() : null;
    }

}

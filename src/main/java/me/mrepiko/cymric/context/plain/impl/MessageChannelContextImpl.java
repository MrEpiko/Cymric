package me.mrepiko.cymric.context.plain.impl;

import me.mrepiko.cymric.context.plain.MessageChannelContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageChannelContextImpl implements MessageChannelContext {

    private final MessageChannelUnion messageChannelUnion;
    private final User user;

    public MessageChannelContextImpl(@NotNull MessageChannel messageChannel, @Nullable User user) {
        this.messageChannelUnion = (MessageChannelUnion) messageChannel;
        this.user = user;
    }

    public MessageChannelContextImpl(@NotNull MessageChannelUnion messageChannelUnion, @Nullable User user) {
        this.messageChannelUnion = messageChannelUnion;
        this.user = user;
    }

    @NotNull
    @Override
    public MessageChannel getMessageChannel() {
        return messageChannelUnion;
    }

    @Nullable
    @Override
    public User getUser() {
        return user;
    }

    @Nullable
    @Override
    public Guild getGuild() {
        if (messageChannelUnion instanceof GuildMessageChannel guildMessageChannel) {
            return guildMessageChannel.getGuild();
        }
        return null;
    }

    @Nullable
    @Override
    public Member getMember() {
        Guild guild = getGuild();
        if (guild == null || user == null) {
            return null;
        }
        return guild.retrieveMember(user).complete();
    }

}

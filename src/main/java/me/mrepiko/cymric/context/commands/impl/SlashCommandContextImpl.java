package me.mrepiko.cymric.context.commands.impl;

import lombok.Getter;
import me.mrepiko.cymric.context.commands.SlashCommandContext;
import me.mrepiko.cymric.elements.command.CommandHolder;
import me.mrepiko.cymric.elements.command.chat.GenericChatCommand;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class SlashCommandContextImpl implements SlashCommandContext {

    private final SlashCommandInteractionEvent event;
    private final GenericChatCommand command;
    private final SlashCommandInteraction interaction;

    public SlashCommandContextImpl(@NotNull SlashCommandInteractionEvent event, @NotNull GenericChatCommand command) {
        this.event = event;
        this.command = command;
        this.interaction = event.getInteraction();
    }

    @Nullable
    @Override
    public Message getMessage() {
        return (event.isAcknowledged()) ? event.getHook().retrieveOriginal().complete() : null;
    }

    @NotNull
    @Override
    public SlashCommandInteraction getInteraction() {
        return interaction;
    }

    @NotNull
    @Override
    public CommandHolder<?> getCommandHolder() {
        return command;
    }

    @Override
    public boolean isUserIntegration() {
        return getMessageChannel().isDetached();
    }

    @NotNull
    @Override
    public MessageChannel getMessageChannel() {
        return event.getChannel();
    }

    @Nullable
    @Override
    public Guild getGuild() {
        return event.getGuild();
    }

    @NotNull
    @Override
    public User getUser() {
        return event.getUser();
    }

    @Nullable
    @Override
    public Member getMember() {
        return event.getMember();
    }

    @Nullable
    @Override
    public String getOptionAsString(@NotNull String option) {
        return getOptionAsString(option, null);
    }

    @Nullable
    @Override
    public String getOptionAsString(@NotNull String option, @Nullable String defaultValue) {
        OptionMapping optionMapping = interaction.getOption(option);
        return (optionMapping == null) ? defaultValue : optionMapping.getAsString();
    }

    @Nullable
    @Override
    public String getOptionAsString(@NotNull String option, int pullArgs) {
        return getOptionAsString(option);
    }

    @Nullable
    @Override
    public String getOptionAsString(@NotNull String option, int pullArgs, @Nullable String defaultValue) {
        return getOptionAsString(option, "");
    }

    @Nullable
    @Override
    public Integer getOptionAsInteger(@NotNull String option) {
        return getOptionAsInteger(option, 0);
    }

    @Nullable
    @Override
    public Integer getOptionAsInteger(@NotNull String option, @Nullable Integer defaultValue) {
        OptionMapping optionMapping = interaction.getOption(option);
        return (optionMapping == null) ? defaultValue : Integer.valueOf(optionMapping.getAsInt());
    }

    @Nullable
    @Override
    public Double getOptionAsDouble(@NotNull String option) {
        return getOptionAsDouble(option, 0.0);
    }

    @Nullable
    @Override
    public Double getOptionAsDouble(@NotNull String option, @Nullable Double defaultValue) {
        OptionMapping optionMapping = interaction.getOption(option);
        return (optionMapping == null) ? defaultValue : Double.valueOf(optionMapping.getAsDouble());
    }

    @Nullable
    @Override
    public Long getOptionAsLong(@NotNull String option) {
        return getOptionAsLong(option, 0L);
    }

    @Nullable
    @Override
    public Long getOptionAsLong(@NotNull String option, @Nullable Long defaultValue) {
        OptionMapping optionMapping = interaction.getOption(option);
        return (optionMapping == null) ? defaultValue : Long.valueOf(optionMapping.getAsLong());
    }

    @Nullable
    @Override
    public Boolean getOptionAsBoolean(@NotNull String option) {
        return getOptionAsBoolean(option, null);
    }

    @Nullable
    @Override
    public Boolean getOptionAsBoolean(@NotNull String option, @Nullable Boolean defaultValue) {
        OptionMapping optionMapping = interaction.getOption(option);
        return (optionMapping == null) ? defaultValue : Boolean.valueOf(optionMapping.getAsBoolean());
    }

    @Nullable
    @Override
    public User getOptionAsUser(@NotNull String option) {
        return getOptionAsUser(option, null);
    }

    @Nullable
    @Override
    public User getOptionAsUser(@NotNull String option, @Nullable User defaultValue) {
        OptionMapping optionMapping = interaction.getOption(option);
        return (optionMapping == null) ? defaultValue : optionMapping.getAsUser();
    }

    @Nullable
    @Override
    public Member getOptionAsMember(@NotNull String option) {
        return getOptionAsMember(option, null);
    }

    @Nullable
    @Override
    public Member getOptionAsMember(@NotNull String option, @Nullable Member defaultValue) {
        OptionMapping optionMapping = interaction.getOption(option);
        if (optionMapping == null) {
            return defaultValue;
        }
        Member member = optionMapping.getAsMember();
        return (member == null) ? defaultValue : member;
    }

    @Nullable
    @Override
    public Channel getOptionAsChannel(@NotNull String option) {
        return getOptionAsChannel(option, null);
    }

    @Nullable
    @Override
    public Channel getOptionAsChannel(@NotNull String option, @Nullable Channel defaultValue) {
        OptionMapping optionMapping = interaction.getOption(option);
        return (optionMapping == null) ? defaultValue : optionMapping.getAsChannel();
    }

    @Nullable
    @Override
    public Role getOptionAsRole(@NotNull String option) {
        return getOptionAsRole(option, null);
    }

    @Nullable
    @Override
    public Role getOptionAsRole(@NotNull String option, @Nullable Role defaultValue) {
        OptionMapping optionMapping = interaction.getOption(option);
        return (optionMapping == null) ? defaultValue : optionMapping.getAsRole();
    }

    @Nullable
    @Override
    public IMentionable getOptionAsMentionable(@NotNull String option) {
        return getOptionAsMentionable(option, null);
    }

    @Nullable
    @Override
    public IMentionable getOptionAsMentionable(@NotNull String option, @Nullable IMentionable defaultValue) {
        OptionMapping optionMapping = interaction.getOption(option);
        return (optionMapping == null) ? defaultValue : optionMapping.getAsMentionable();
    }

    @Nullable
    @Override
    public Message.Attachment getOptionAsAttachment(@NotNull String option) {
        return getOptionAsAttachment(option, null);
    }

    @Nullable
    @Override
    public Message.Attachment getOptionAsAttachment(@NotNull String option, @Nullable Message.Attachment defaultValue) {
        OptionMapping optionMapping = interaction.getOption(option);
        return (optionMapping == null) ? defaultValue : optionMapping.getAsAttachment();
    }
}

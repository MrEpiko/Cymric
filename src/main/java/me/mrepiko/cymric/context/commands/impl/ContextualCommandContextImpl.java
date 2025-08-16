package me.mrepiko.cymric.context.commands.impl;

import lombok.Getter;
import me.mrepiko.cymric.context.commands.ContextualCommandContext;
import me.mrepiko.cymric.elements.command.CommandLoader;
import me.mrepiko.cymric.elements.command.contextual.GenericContextualCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.context.ContextInteraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class ContextualCommandContextImpl implements ContextualCommandContext {

    private final GenericContextInteractionEvent<?> event;
    private final GenericContextualCommand command;
    private final ContextInteraction<?> interaction;

    @Nullable
    private final Message selectedMessage;
    @Nullable
    private final User selectedUser;
    @Nullable
    private final Member selectedMember;

    public ContextualCommandContextImpl(
            GenericContextInteractionEvent<?> event,
            GenericContextualCommand command,
            @Nullable Message selectedMessage,
            @Nullable User selectedUser,
            @Nullable Member selectedMember
    ) {
        this.event = event;
        this.command = command;
        this.interaction = event.getInteraction();
        this.selectedMessage = selectedMessage;
        this.selectedUser = selectedUser;
        this.selectedMember = selectedMember;
    }

    @NotNull
    @Override
    public MessageChannel getMessageChannel() {
        return event.getMessageChannel();
    }

    @NotNull
    @Override
    public CommandLoader<?> getCommandHolder() {
        return command;
    }

    @Override
    public boolean isUserIntegration() {
        return getMessageChannel().isDetached();
    }

    @NotNull
    @Override
    public User getUser() {
        return event.getUser();
    }

    @Nullable
    @Override
    public Guild getGuild() {
        return event.getGuild();
    }

    @Nullable
    @Override
    public Member getMember() {
        return event.getMember();
    }
}

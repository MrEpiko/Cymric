package me.mrepiko.cymric.context.commands;

import me.mrepiko.cymric.context.ReplyCallbackContext;
import me.mrepiko.cymric.context.plain.MessageChannelContext;
import me.mrepiko.cymric.elements.command.contextual.ContextualCommandHandler;
import me.mrepiko.cymric.elements.command.contextual.GenericContextualCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericContextInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.context.ContextInteraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ContextualCommandContext extends CommandContext, MessageChannelContext, ReplyCallbackContext {
    @Nullable
    Message getSelectedMessage();

    @Nullable
    User getSelectedUser();

    @Nullable
    Member getSelectedMember();

    @NotNull
    ContextualCommandHandler getCommand();

    @NotNull
    GenericContextInteractionEvent<?> getEvent();

    @NotNull
    ContextInteraction<?> getInteraction();

    @NotNull
    MessageChannel getMessageChannel();

    @NotNull
    @Override
    default IReplyCallback getReplyCallback() {
        return getInteraction();
    }
}

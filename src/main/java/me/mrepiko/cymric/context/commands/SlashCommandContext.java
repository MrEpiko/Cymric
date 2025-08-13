package me.mrepiko.cymric.context.commands;

import me.mrepiko.cymric.context.ReplyCallbackContext;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SlashCommandContext extends ChatCommandContext, ReplyCallbackContext {
    @NotNull
    SlashCommandInteractionEvent getEvent();

    @Nullable
    Message getMessage();

    @NotNull
    SlashCommandInteraction getInteraction();

    @NotNull
    @Override
    default IReplyCallback getReplyCallback() {
        return getInteraction();
    }
}

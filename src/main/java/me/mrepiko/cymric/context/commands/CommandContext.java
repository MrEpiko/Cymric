package me.mrepiko.cymric.context.commands;

import me.mrepiko.cymric.elements.command.CommandHandler;

import org.jetbrains.annotations.NotNull;

public interface CommandContext {
    @NotNull
    CommandHandler<?> getCommandHandler();

    boolean isUserIntegration();
}

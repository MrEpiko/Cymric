package me.mrepiko.cymric.context.commands;

import me.mrepiko.cymric.elements.command.CommandHolder;
import org.jetbrains.annotations.NotNull;

public interface CommandContext {
    @NotNull
    CommandHolder<?> getCommandHolder();

    boolean isUserIntegration();
}

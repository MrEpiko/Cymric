package me.mrepiko.cymric.context.commands;

import me.mrepiko.cymric.elements.command.CommandLoader;
import org.jetbrains.annotations.NotNull;

public interface CommandContext {
    @NotNull
    CommandLoader<?> getCommandHolder();

    boolean isUserIntegration();
}

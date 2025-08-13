package me.mrepiko.cymric.elements.command.contextual;

import me.mrepiko.cymric.context.commands.ContextualCommandContext;
import me.mrepiko.cymric.elements.command.CommandTemplate;
import org.jetbrains.annotations.NotNull;

public interface ContextualCommandTemplate extends CommandTemplate {
    void onInteraction(@NotNull ContextualCommandContext context);
}

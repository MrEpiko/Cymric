package me.mrepiko.cymric.elements.command.contextual;

import me.mrepiko.cymric.context.commands.ContextualCommandContext;
import me.mrepiko.cymric.elements.command.CommandHandler;
import me.mrepiko.cymric.elements.command.chat.data.ForgedChatCommandData;
import me.mrepiko.cymric.elements.command.contextual.data.ForgedContextualCommandData;
import me.mrepiko.cymric.elements.containers.ElementDataContainer;
import me.mrepiko.cymric.elements.plain.Conditionable;
import org.jetbrains.annotations.NotNull;

public interface ContextualCommandHandler extends CommandHandler<ForgedContextualCommandData> {
    void onInteraction(@NotNull ContextualCommandContext context);
}

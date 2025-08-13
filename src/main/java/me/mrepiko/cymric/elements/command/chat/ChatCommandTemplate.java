package me.mrepiko.cymric.elements.command.chat;

import me.mrepiko.cymric.context.commands.ChatCommandContext;
import org.jetbrains.annotations.NotNull;

public interface ChatCommandTemplate {
    void onInteraction(@NotNull ChatCommandContext context);

    /**
     * Called upon incorrect usage of the command.
     * This method has to be invoked upon incorrectly inputted args in context of PREFIX command.
     * If command is a slash command, there is no need to invoke this method as Discord won't allow command to be executed.
     *
     * @param context The context of the chat command interaction.
     */
    void onIncorrectUsage(@NotNull ChatCommandContext context);

    @NotNull
    CommandFunctionalityType getType();
}

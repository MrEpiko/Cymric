package me.mrepiko.cymric.elements.command.chat;

import me.mrepiko.cymric.context.commands.ChatCommandContext;
import me.mrepiko.cymric.elements.command.CommandHandler;
import me.mrepiko.cymric.elements.command.chat.data.ForgedChatCommandData;
import me.mrepiko.cymric.elements.containers.ElementDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ChatCommandHandler extends CommandHandler<ForgedChatCommandData> {

    @Nullable
    ChatCommandHandler getParentCommand();

    void setParentCommand(@Nullable ChatCommandHandler parentCommand);

    @Nullable
    List<ChatCommandHandler> getChildrenCommands();

    void setChildrenCommands(@Nullable List<ChatCommandHandler> childrenCommands);

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


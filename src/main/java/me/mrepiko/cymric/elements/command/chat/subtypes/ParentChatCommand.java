package me.mrepiko.cymric.elements.command.chat.subtypes;

import me.mrepiko.cymric.context.commands.ChatCommandContext;
import me.mrepiko.cymric.elements.command.chat.CommandFunctionalityType;
import me.mrepiko.cymric.elements.command.chat.GenericChatCommand;
import org.jetbrains.annotations.NotNull;

/**
 * A command that serves as a parent for other slash commands.
 * It does not perform any action on its own and is used to group related commands.
 * To register this command, you must input {@code [command_id].json} file in the {@code parent_commands} folder.
 */
public class ParentChatCommand extends GenericChatCommand {

    public ParentChatCommand(@NotNull String id) {
        super(id, CommandFunctionalityType.PARENT);
    }

    @Override
    public void onInteraction(@NotNull ChatCommandContext context) { }

}

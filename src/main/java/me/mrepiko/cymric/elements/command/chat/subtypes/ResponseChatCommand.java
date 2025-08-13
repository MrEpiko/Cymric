package me.mrepiko.cymric.elements.command.chat.subtypes;

import me.mrepiko.cymric.context.commands.ChatCommandContext;
import me.mrepiko.cymric.elements.command.chat.CommandFunctionalityType;
import me.mrepiko.cymric.elements.command.chat.GenericChatCommand;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.response.ResponseBuilder;
import me.mrepiko.cymric.response.data.ResponseData;
import org.jetbrains.annotations.NotNull;

/**
 * A simple command that sends a response to its invoker. Does not require any code implementation.
 * In order to register this command, you must input {@code [command_id].json} file in the {@code response_commands} folder.
 * */
public class ResponseChatCommand extends GenericChatCommand {

    public ResponseChatCommand(@NotNull String id) {
        super(id, CommandFunctionalityType.RESPONSE);
    }

    private final ResponseData responseData = new ResponseData();

    @Override
    public void onInteraction(@NotNull ChatCommandContext context) {
        PlaceholderMap map = context.getPlaceholderMap();
        ResponseBuilder.create(map, responseData).buildAndSend();
    }
}

package me.mrepiko.cymric.elements.command.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.mrepiko.cymric.mics.Constants;

@AllArgsConstructor
@Getter
public enum CommandFunctionalityType {
    NORMAL(Constants.NORMAL_CHAT_COMMAND_CONFIGURATION_FOLDER_PATH),
    RESPONSE(Constants.RESPONSE_CHAT_COMMAND_CONFIGURATION_FOLDER_PATH),
    PARENT(Constants.PARENT_CHAT_COMMAND_CONFIGURATION_FOLDER_PATH);

    private final String configurationFolderPath;
}

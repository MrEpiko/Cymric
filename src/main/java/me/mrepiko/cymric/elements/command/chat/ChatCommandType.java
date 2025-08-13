package me.mrepiko.cymric.elements.command.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChatCommandType {
    HYBRID(), // Combination of both slash and prefix commands.
    SLASH(),
    PREFIX()
}

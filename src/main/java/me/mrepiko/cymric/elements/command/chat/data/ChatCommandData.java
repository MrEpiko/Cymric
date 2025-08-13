package me.mrepiko.cymric.elements.command.chat.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.mrepiko.cymric.elements.command.chat.ChatCommandType;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatCommandData {

    private List<ChatCommandOptionData> options;
    private List<String> childrenIds;
    private ChatCommandType commandType = ChatCommandType.SLASH;

}

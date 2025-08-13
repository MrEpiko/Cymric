package me.mrepiko.cymric.elements.command.chat.data;

import lombok.*;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.mics.Utils;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatCommandChoiceData {

    private String name;
    private String value;

    public Command.Choice getAssembled(@Nullable PlaceholderMap map) {
        return new Command.Choice(
                Utils.applyPlaceholders(map, name),
                Utils.applyPlaceholders(map, value)
        );
    }

}

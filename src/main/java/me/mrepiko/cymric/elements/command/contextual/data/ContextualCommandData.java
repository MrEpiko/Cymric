package me.mrepiko.cymric.elements.command.contextual.data;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.interactions.commands.Command;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ContextualCommandData {

    private Command.Type contextType;

}

package me.mrepiko.cymric.elements.command.data;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

// I cannot stand conflicting imports, so this is supposed to take care of that.
@AllArgsConstructor
@Setter
public class JdaCommandData {

    @Delegate
    private CommandData commandData;

    public CommandData get() {
        return commandData;
    }

    public SlashCommandData getAsSlashCommandData() {
        if (commandData instanceof SlashCommandData slashCommandData) {
            return slashCommandData;
        }
        throw new IllegalStateException("Command data is not a SlashCommandData instance");
    }

}

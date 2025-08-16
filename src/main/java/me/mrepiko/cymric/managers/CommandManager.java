package me.mrepiko.cymric.managers;

import me.mrepiko.cymric.elements.command.CommandLoader;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

public interface CommandManager extends ElementManager<CommandLoader<?>> {
    void registerGlobalCommands();
    void registerGuildCommands(@NotNull Guild guild);
}

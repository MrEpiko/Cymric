package me.mrepiko.cymric.managers;

import me.mrepiko.cymric.elements.command.CommandHolder;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

public interface CommandManager extends ElementManager<CommandHolder<?>> {
    void registerGlobalCommands();
    void registerGuildCommands(@NotNull Guild guild);
}

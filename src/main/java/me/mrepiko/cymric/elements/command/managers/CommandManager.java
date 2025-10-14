package me.mrepiko.cymric.elements.command.managers;

import me.mrepiko.cymric.elements.command.CommandHandler;
import me.mrepiko.cymric.elements.managers.ElementManager;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

public interface CommandManager extends ElementManager<CommandHandler<?>> {
    void registerGlobalCommands();
    void registerGuildCommands(@NotNull Guild guild);
}

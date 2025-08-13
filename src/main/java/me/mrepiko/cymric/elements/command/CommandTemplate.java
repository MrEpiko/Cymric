package me.mrepiko.cymric.elements.command;

import me.mrepiko.cymric.elements.command.data.CommandData;
import me.mrepiko.cymric.elements.command.data.JdaCommandData;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CommandTemplate {
    @NotNull
    List<JdaCommandData> getJdaCommandData(@Nullable PlaceholderMap map);

    @NotNull
    CommandData getCommandData();

    @Nullable
    Command getDiscordCommand();

    void setDiscordCommand(@Nullable Command command);

    @NotNull
    String getFullName(); // Used for command registration and retrieval.
}

package me.mrepiko.cymric.elements.command;

import me.mrepiko.cymric.elements.command.data.CommandData;
import me.mrepiko.cymric.elements.command.data.JdaCommandData;
import me.mrepiko.cymric.elements.plain.Conditionable;
import me.mrepiko.cymric.elements.plain.SerializableBotElement;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import net.dv8tion.jda.api.interactions.commands.ICommandReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CommandHandler<T extends ForgedCommandDataContainer> extends SerializableBotElement<T>, Conditionable {
    @NotNull
    List<JdaCommandData> getJdaCommandData(@Nullable PlaceholderMap map);

    @NotNull
    CommandData getCommandData();

    @Nullable
    ICommandReference getDiscordCommand();

    void setDiscordCommand(@Nullable ICommandReference command);

    @NotNull
    String getFullName(); // Used for command registration and retrieval.
}

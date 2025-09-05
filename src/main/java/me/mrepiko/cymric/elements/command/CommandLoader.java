package me.mrepiko.cymric.elements.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.mrepiko.cymric.elements.ConditionalElementLoader;
import me.mrepiko.cymric.elements.command.data.CommandData;
import me.mrepiko.cymric.elements.command.data.JdaCommandData;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import net.dv8tion.jda.api.interactions.commands.ICommandReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public abstract class CommandLoader<T extends ForgedCommandDataContainer> extends ConditionalElementLoader<T> implements CommandHandler<T> {

    @Setter(AccessLevel.PROTECTED)
    private CommandData commandData;
    @Setter
    private ICommandReference discordCommand;

    public CommandLoader(@NotNull String id, @NotNull String folderPath) {
        super(id, folderPath);
    }

    @NotNull
    @Override
    public abstract String getFullName();

    @NotNull
    public abstract List<JdaCommandData> getJdaCommandData(@Nullable PlaceholderMap map);
}

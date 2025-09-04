package me.mrepiko.cymric.elements.command.contextual;

import lombok.Getter;
import me.mrepiko.cymric.context.commands.ContextualCommandContext;
import me.mrepiko.cymric.elements.ElementError;
import me.mrepiko.cymric.elements.command.CommandLoader;
import me.mrepiko.cymric.elements.command.contextual.data.ForgedContextualCommandData;
import me.mrepiko.cymric.elements.command.data.JdaCommandData;
import me.mrepiko.cymric.mics.Constants;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public abstract class GenericContextualCommand extends CommandLoader<ForgedContextualCommandData> implements ContextualCommandHandler {

    public GenericContextualCommand(@NotNull String id) {
        super(id, Constants.CONTEXTUAL_COMMAND_CONFIGURATION_FOLDER_PATH);
    }

    @Override
    public void reload() {
        super.reload();
        if (this.configMissing) {
            return;
        }
        super.setCommandData(data.getCommandData());
        super.setConditionalData(data.getConditionalData(), "contextual_command", ElementError.getAllWithout(ElementError.INVALID_ARGS));
    }

    @Override
    public void initializeData() {
        ForgedContextualCommandData emptyData = new ForgedContextualCommandData();
        this.data = config.getOrSetDefault("properties", ForgedContextualCommandData.class, emptyData);
        if (this.data == emptyData) {
            this.configMissing = true;
        }
    }

    @NotNull
    @Override
    public List<JdaCommandData> getJdaCommandData(@Nullable PlaceholderMap map) {
        return data.getJdaCommandData(map);
    }

    @NotNull
    @Override
    public String getFullName() {
        return data.getName();
    }

    @Override
    public abstract void onInteraction(@NotNull ContextualCommandContext context);
}

package me.mrepiko.cymric.elements.command.contextual;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import me.mrepiko.cymric.config.ConfigFile;
import me.mrepiko.cymric.context.commands.ContextualCommandContext;
import me.mrepiko.cymric.elements.ElementError;
import me.mrepiko.cymric.elements.command.CommandHolder;
import me.mrepiko.cymric.elements.command.contextual.data.ForgedContextualCommandData;
import me.mrepiko.cymric.elements.command.data.JdaCommandData;
import me.mrepiko.cymric.jackson.JacksonUtils;
import me.mrepiko.cymric.jackson.JsonContainer;
import me.mrepiko.cymric.mics.Constants;
import me.mrepiko.cymric.mics.Utils;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public abstract class GenericContextualCommand extends CommandHolder<ForgedContextualCommandData> implements ContextualCommandTemplate {

    private final String filePath;

    protected final String id;
    @Delegate
    protected JsonContainer config;
    protected ForgedContextualCommandData data;

    @Nullable
    @Setter
    private Command discordCommand;

    private boolean configMissing;

    public GenericContextualCommand(@NotNull String id) {
        this.id = id;
        this.filePath = Constants.CONTEXTUAL_COMMAND_CONFIGURATION_FOLDER_PATH + id + ".json";
        if (!Utils.isFileExists(this.filePath)) {
            this.configMissing = true;
        }
    }

    @Override
    public void reload() {
        if (this.configMissing) {
            return;
        }
        this.config = new JsonContainer(new ConfigFile(this.filePath));
        setupConfig();
        JacksonUtils.mergeDeclaredFieldsFromJson(this, config);
        if (this.configMissing) {
            return;
        }
        super.setCommandData(data.getCommandData());
        super.setConditionalData(data.getConditionalData(), "contextual_command", ElementError.getAllWithout(ElementError.INVALID_ARGS));
    }

    private void setupConfig() {
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

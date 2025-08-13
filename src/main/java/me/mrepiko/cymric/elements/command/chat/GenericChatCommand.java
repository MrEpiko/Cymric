package me.mrepiko.cymric.elements.command.chat;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import me.mrepiko.cymric.config.ConfigFile;
import me.mrepiko.cymric.context.commands.ChatCommandContext;
import me.mrepiko.cymric.elements.ElementError;
import me.mrepiko.cymric.elements.command.CommandHolder;
import me.mrepiko.cymric.elements.command.chat.data.ForgedChatCommandData;
import me.mrepiko.cymric.elements.command.data.JdaCommandData;
import me.mrepiko.cymric.jackson.JacksonUtils;
import me.mrepiko.cymric.jackson.JsonContainer;
import me.mrepiko.cymric.mics.Utils;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.response.ResponseBuilder;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public abstract class GenericChatCommand extends CommandHolder<ForgedChatCommandData> implements ChatCommandTemplate {

    private final String filePath;

    protected final String id;
    @Delegate
    protected JsonContainer config;
    protected ForgedChatCommandData data;

    private final CommandFunctionalityType type;

    @Nullable
    @Setter
    private Command discordCommand;

    private boolean configMissing;

    public GenericChatCommand(@NotNull String id) {
        this(id, CommandFunctionalityType.NORMAL);
    }

    public GenericChatCommand(@NotNull String id, @NotNull CommandFunctionalityType type) {
        this.id = id;
        this.type = type;
        this.filePath = type.getConfigurationFolderPath() + id + ".json";
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
        super.setConditionalData(data.getConditionalData(), "chat_command", ElementError.getAll());
    }

    private void setupConfig() {
        ForgedChatCommandData emptyData = new ForgedChatCommandData();
        this.data = config.getOrSetDefault("properties", ForgedChatCommandData.class, emptyData);
        if (this.data == emptyData) {
            this.configMissing = true;
        }
    }

    @NotNull
    @Override
    public List<JdaCommandData> getJdaCommandData(@Nullable PlaceholderMap map) {
        return data.getCommandData(map);
    }

    @NotNull
    @Override
    public String getFullName() {
        return data.getFullName();
    }

    @Override
    public void onIncorrectUsage(@NotNull ChatCommandContext context) {
        PlaceholderMap map = context.getPlaceholderMap();
        map.put("usage", data.getUsage());
        map.put("arg_explanation", data.getArgExplanation());
        ResponseBuilder.create(map, getErrorResponseData(ElementError.INVALID_ARGS)).buildAndSend();
    }

    @Override
    public abstract void onInteraction(@NotNull ChatCommandContext context);

}

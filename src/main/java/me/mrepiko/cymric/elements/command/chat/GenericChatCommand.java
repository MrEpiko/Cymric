package me.mrepiko.cymric.elements.command.chat;

import lombok.Getter;
import me.mrepiko.cymric.context.commands.ChatCommandContext;
import me.mrepiko.cymric.elements.ElementError;
import me.mrepiko.cymric.elements.command.CommandLoader;
import me.mrepiko.cymric.elements.command.chat.data.ForgedChatCommandData;
import me.mrepiko.cymric.elements.command.data.JdaCommandData;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.response.ResponseBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public abstract class GenericChatCommand extends CommandLoader<ForgedChatCommandData> implements ChatCommandTemplate {

    private final CommandFunctionalityType type;

    public GenericChatCommand(@NotNull String id) {
        this(id, CommandFunctionalityType.NORMAL);
    }

    public GenericChatCommand(@NotNull String id, @NotNull CommandFunctionalityType type) {
        super(id, type.getConfigurationFolderPath());
        this.type = type;
    }

    @Override
    public void reload() {
        super.reload();
        if (this.configMissing) {
            return;
        }
        super.setCommandData(data.getCommandData());
        super.setConditionalData(data.getConditionalData(), "chat_command", ElementError.getAll());
    }

    @Override
    public void initializeData() {
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

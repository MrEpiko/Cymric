package me.mrepiko.cymric.elements.command.chat;

import lombok.Getter;
import lombok.Setter;
import me.mrepiko.cymric.context.commands.ChatCommandContext;
import me.mrepiko.cymric.elements.ElementError;
import me.mrepiko.cymric.elements.command.CommandLoader;
import me.mrepiko.cymric.elements.command.chat.data.ForgedChatCommandData;
import me.mrepiko.cymric.elements.command.data.JdaCommandData;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.response.ResponseBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class GenericChatCommand extends CommandLoader<ForgedChatCommandData> implements ChatCommandHandler {

    private final CommandFunctionalityType type;

    @Setter
    private ChatCommandHandler parentCommand;
    @Setter
    private List<ChatCommandHandler> childrenCommands = new ArrayList<>();

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
        return data.getCommandData(this, map);
    }

    @NotNull
    @Override
    public String getFullName() {
        return data.getFullName(this);
    }

    @Override
    public void onIncorrectUsage(@NotNull ChatCommandContext context) {
        PlaceholderMap map = context.getPlaceholderMap();
        map.put("usage", data.getUsage(this));
        map.put("arg_explanation", data.getArgExplanation());
        ResponseBuilder.create(map, getErrorResponseData(ElementError.INVALID_ARGS)).buildAndSend();
    }

    @Override
    public abstract void onInteraction(@NotNull ChatCommandContext context);

}


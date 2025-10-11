package me.mrepiko.cymric.response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kotlin.Pair;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.mrepiko.cymric.CymricApi;
import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.context.plain.MessageChannelContext;
import me.mrepiko.cymric.context.plain.MessageContext;
import me.mrepiko.cymric.context.plain.impl.MessageChannelContextImpl;
import me.mrepiko.cymric.context.plain.impl.MessageContextImpl;
import me.mrepiko.cymric.discord.DiscordCache;
import me.mrepiko.cymric.elements.components.ComponentHandler;
import me.mrepiko.cymric.elements.components.button.ButtonHandler;
import me.mrepiko.cymric.elements.components.button.data.ForgedButtonData;
import me.mrepiko.cymric.elements.components.selectmenus.entityselect.EntitySelectMenuHandler;
import me.mrepiko.cymric.elements.components.selectmenus.stringselect.StringSelectMenuHandler;
import me.mrepiko.cymric.elements.components.selectmenus.stringselect.data.ForgedStringSelectMenuData;
import me.mrepiko.cymric.elements.components.selectmenus.stringselect.data.StringSelectMenuOptionData;
import me.mrepiko.cymric.elements.modal.ModalHandler;
import me.mrepiko.cymric.elements.modal.data.ForgedModalData;
import me.mrepiko.cymric.jackson.JacksonUtils;
import me.mrepiko.cymric.jackson.JsonContainer;
import me.mrepiko.cymric.managers.ComponentManager;
import me.mrepiko.cymric.managers.ModalManager;
import me.mrepiko.cymric.mics.Utils;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.placeholders.PlaceholderMapBuilder;
import me.mrepiko.cymric.response.data.ActionData;
import me.mrepiko.cymric.response.data.components.ActionButtonData;
import me.mrepiko.cymric.response.data.components.ActionEntitySelectMenuData;
import me.mrepiko.cymric.response.data.components.ActionModalData;
import me.mrepiko.cymric.response.data.components.ActionStringSelectMenuData;
import me.mrepiko.cymric.response.data.embed.FieldData;
import me.mrepiko.cymric.response.data.embed.MessageEmbedData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Action {

    private final CymricApi instance = DiscordBot.getInstance();
    private final ComponentManager componentManager = instance.getComponentManager();
    private final ModalManager modalManager = instance.getModalManager();

    private final ActionData data;

    @Nullable
    private MessageChannel messageChannel;
    @Nullable
    private Message message;

    private final List<File> files = new ArrayList<>();

    @Setter(AccessLevel.PACKAGE)
    private boolean channelOrMessageProvided = false;

    // Class that extends ComponentHolder: Data (ButtonData, StringSelectMenuData, etc.)
    private final List<Pair<ComponentHandler<?>, Object>> components = new ArrayList<>();

    // Class that extends Modal: ModalData
    @Nullable
    private Pair<? extends ModalHandler, ForgedModalData> modal;

    public void setMessageChannel(@NotNull MessageChannel messageChannel) {
        this.messageChannel = messageChannel;
        this.channelOrMessageProvided = true;
    }

    public void setMessage(@NotNull Message message) {
        this.message = message;
        this.channelOrMessageProvided = true;
    }

    public void setModal(@NotNull Class<? extends ModalHandler> clazz, @NotNull ForgedModalData modalData) {
        ModalHandler modalHandler = modalManager.getByClass(clazz);
        this.modal = new Pair<>(modalHandler, modalData);
    }

    public void addButton(@NotNull Class<? extends ButtonHandler> clazz, @NotNull ForgedButtonData data) {
        addComponent(clazz, data);
    }

    public void addStringSelectMenu(@NotNull Class<? extends StringSelectMenuHandler> clazz, @NotNull Object data) {
        addComponent(clazz, data);
    }

    public void addEntitySelectMenu(@NotNull Class<? extends EntitySelectMenuHandler> clazz, @NotNull Object data) {
        addComponent(clazz, data);
    }

    private void addComponent(@NotNull Class<? extends ComponentHandler<?>> clazz, @NotNull Object data) {
        ComponentManager componentManager = instance.getComponentManager();
        ComponentHandler<?> handler = componentManager.getByClass(clazz);
        this.components.add(new Pair<>(handler, data));
    }

    public void injectEmbedFields(int embedIndex, @NotNull List<FieldData> fieldsData) {
        List<MessageEmbedData> embeds = data.getEmbeds();
        if (embeds == null || embeds.size() <= embedIndex) {
            throw new IndexOutOfBoundsException("Embed index out of bounds");
        }
        MessageEmbedData embedData = embeds.get(embedIndex);
        if (embedData == null) {
            throw new IllegalArgumentException("No MessageEmbedData found at index: " + embedIndex);
        }
        embedData.setFields(fieldsData);
    }

    public void injectStringSelectMenuOptions(@NotNull Class<? extends StringSelectMenuHandler> clazz, @NotNull List<StringSelectMenuOptionData> options) {
        ComponentHandler<?> handler = componentManager.getByClass(clazz);
        for (Pair<ComponentHandler<?>, Object> pair : components) {
            if (pair.getFirst() != handler) {
                continue;
            }
            ForgedStringSelectMenuData component = (ForgedStringSelectMenuData) pair.getSecond();
            component.setOptions(options);
            return;
        }
        throw new IllegalArgumentException("No StringSelectMenuData found for class: " + clazz.getName());
    }

    @NotNull
    protected PlaceholderMap initialize(@NotNull PlaceholderMap map) {
        initializeFiles();
        initializeModal();
        initializeComponents();
        return initializeValues(map);
    }

    private void initializeFiles() {
        List<String> filePaths = data.getFilePaths();
        if (filePaths == null || filePaths.isEmpty()) {
            return;
        }
        filePaths.forEach(filePath -> {
            File file = new File(filePath);
            if (!file.exists()) {
                DiscordBot.getLogger().warn("File not found: {}", filePath);
                return;
            }
            files.add(file);
        });
    }

    /**
     * Initializes the modal for this Action based on ActionData.
     * If modal is already provided, this will be skipped.-
     */
    private void initializeModal() {
        if (modal != null) {
            return;
        }

        ObjectNode objectNode = data.getModal();
        if (objectNode == null) {
            return;
        }

        JsonContainer container = new JsonContainer(objectNode);
        ActionModalData actionModalData = container.getAs(ActionModalData.class);

        String id = actionModalData.getId();
        if (id == null || id.isEmpty()) {
            DiscordBot.getLogger().warn("Modal ID is not provided in ActionData, skipping modal initialization.");
            return;
        }

        ModalHandler modalHandler = modalManager.getById(id);
        ForgedModalData deepCopy;
        try {
            deepCopy = (ForgedModalData) JacksonUtils.deepCopy(modalHandler.getData());
        } catch (IOException e) {
            throw new RuntimeException("Failed to deep copy modal data", e);
        }

        JacksonUtils.mergeDeclaredFieldsFromJson(
                deepCopy,
                container,
                false
        );

        this.modal = new Pair<>(modalHandler, deepCopy);
    }

    /**
     * Initializes components for this Action based on ActionData.
     * If modal is already provided, this will be skipped.
     */
    private void initializeComponents() {
        if (!components.isEmpty()) {
            return;
        }
        List<ObjectNode> buttons = data.getButtons();
        List<ObjectNode> stringSelectMenus = data.getStringSelectMenus();
        List<ObjectNode> entitySelectMenus = data.getEntitySelectMenus();

        if (buttons != null) {
            initializeComponents(
                    buttons,
                    ActionButtonData::getId,
                    container -> container.getAs(ActionButtonData.class)
            );
        }
        if (stringSelectMenus != null) {
            initializeComponents(
                    stringSelectMenus,
                    ActionStringSelectMenuData::getId,
                    container -> container.getAs(ActionStringSelectMenuData.class)
            );
        }
        if (entitySelectMenus != null) {
            initializeComponents(
                    entitySelectMenus,
                    ActionEntitySelectMenuData::getId,
                    container -> container.getAs(ActionEntitySelectMenuData.class)
            );
        }
    }

    // A - ActionData
    // F - ForgedData
    @SuppressWarnings("unchecked")
    private <A, F> void initializeComponents(
            @NotNull List<ObjectNode> items,
            @NotNull Function<A, String> idExtractor,
            @NotNull Function<JsonContainer, A> dataExtractor
    ) {
        for (ObjectNode objectNode : items) {
            JsonContainer container = new JsonContainer(objectNode);
            A actionData = dataExtractor.apply(container);

            String id = idExtractor.apply(actionData);
            if (id == null || id.isEmpty()) {
                DiscordBot.getLogger().warn("Component ID is not provided in ActionData, skipping component initialization.");
                continue;
            }

            ComponentHandler<?> handler = componentManager.getById(id);
            F deepCopy;
            try {
                deepCopy = (F) JacksonUtils.deepCopy(handler.getData());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            JacksonUtils.mergeDeclaredFieldsFromJson(
                    deepCopy,
                    container,
                    false
            );

            components.add(new Pair<>(handler, deepCopy));
        }
    }

    /**
     * Initializes the values of the ActionData based on the provided PlaceholderMap.
     * It has to be called after the ResponseData is deserialized.
     *
     * @param map The PlaceholderMap to initialize the values with.
     */
    @NotNull
    private PlaceholderMap initializeValues(@NotNull PlaceholderMap map) {
        CymricApi instance = DiscordBot.getInstance();
        MessageChannelContext newMapContext;
        MessageChannelContext context = map.getContext();

        if (data.isInheritMessage() && message != null) { // If message has been inherited from the previous response.
            newMapContext = new MessageContextImpl(message);
            channelOrMessageProvided = true;
            return PlaceholderMapBuilder.create(newMapContext, map).build();
        }

        String guildId = Utils.applyPlaceholders(map, data.getGuildId());
        String channelId = Utils.applyPlaceholders(map, data.getChannelId());
        String messageId = Utils.applyPlaceholders(map, data.getMessageId());
        boolean forceMessage = data.isForceMessage();
        if (channelId.isEmpty() && context == null) {
            throw new IllegalArgumentException("Either channelId or messageChannelUnion must be provided");
        }

        MessageChannel originalChannel = (context == null) ? null : context.getMessageChannel();
        boolean channelIdProvided = !channelId.isEmpty();
        boolean equalsToContextChannel = context != null && context.getMessageChannel().getId().equalsIgnoreCase(channelId);

        if (channelIdProvided) {
            if (equalsToContextChannel) {
                messageChannel = context.getMessageChannel();
            } else {
                if (guildId.isEmpty()) {
                    throw new IllegalArgumentException("If channelId is provided and is not equal to the context channel, guildId must be provided");
                }
                Guild guild = instance.getShardManager().getGuildById(guildId);
                if (guild == null) {
                    throw new IllegalArgumentException("Guild with ID " + guildId + " not found");
                }
                messageChannel = guild.getTextChannelById(channelId);
            }
        } else {
            messageChannel = originalChannel;
        }

        if (messageChannel == null) {
            throw new IllegalArgumentException("MessageChannel with ID " + channelId + " not found");
        }

        if (messageId.isEmpty()) {
            if (messageChannel == originalChannel && !channelIdProvided) { // If both channel nor message are provided in data, don't change the map.
                if (context instanceof MessageContext messageContext) {
                    message = messageContext.getMessage();
                }
                return map;
            }
            // If messageId is not provided, we will use the channel to create a new MessageChannelContext.
            newMapContext = new MessageChannelContextImpl(messageChannel, (context == null) ? null : context.getUser());
        } else {
            // If messageId is provided, we will fetch the message from the cache or JDA.
            message = DiscordCache.getMessageById(messageId, messageChannel, forceMessage);
            newMapContext = new MessageContextImpl(message);
        }

        channelOrMessageProvided = true;
        return PlaceholderMapBuilder.create(newMapContext, map).build();
    }

    public boolean isEmpty() {
        List<MessageEmbedData> embeds = data.getEmbeds();
        String content = data.getContent();
        return (embeds == null || embeds.isEmpty())
                && (content == null || content.isEmpty())
                && files.isEmpty()
                && components.isEmpty()
                && (modal == null || modal.getFirst() == null);
    }

}

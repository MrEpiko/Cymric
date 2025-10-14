package me.mrepiko.cymric.response;

import kotlin.Pair;
import lombok.Getter;
import me.mrepiko.cymric.CymricApi;
import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.context.ReplyCallbackContext;
import me.mrepiko.cymric.context.commands.SlashCommandContext;
import me.mrepiko.cymric.context.components.ComponentContext;
import me.mrepiko.cymric.context.modal.ModalContext;
import me.mrepiko.cymric.context.plain.MessageChannelContext;
import me.mrepiko.cymric.discord.DiscordCache;
import me.mrepiko.cymric.elements.components.ComponentHandler;
import me.mrepiko.cymric.elements.components.ComponentLoader;
import me.mrepiko.cymric.elements.components.ForgedComponentDataContainer;
import me.mrepiko.cymric.elements.components.RowComponent;
import me.mrepiko.cymric.elements.modal.ModalHandler;
import me.mrepiko.cymric.elements.modal.data.ForgedModalData;
import me.mrepiko.cymric.elements.components.managers.ComponentManager;
import me.mrepiko.cymric.elements.modal.managers.ModalManager;
import me.mrepiko.cymric.elements.components.managers.runtime.RuntimeComponent;
import me.mrepiko.cymric.elements.managers.runtime.RuntimeExtra;
import me.mrepiko.cymric.elements.tasks.managers.runtime.RuntimeModal;
import me.mrepiko.cymric.elements.components.managers.runtime.RuntimeComponentImpl;
import me.mrepiko.cymric.elements.tasks.managers.runtime.RuntimeModalImpl;
import me.mrepiko.cymric.mics.Utils;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.response.data.ActionData;
import me.mrepiko.cymric.response.data.embed.MessageEmbedData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ResponseImpl implements Response {

    private final CymricApi instance = DiscordBot.getInstance();
    private final ComponentManager componentManager = instance.getComponentManager();
    private final ModalManager modalManager = instance.getModalManager();
    private final Logger logger = DiscordBot.getLogger();

    @Getter
    private final PlaceholderMap map;
    @Getter
    private final Action action;
    private final ActionData data;
    @Getter
    private final ResponseChain responseChain;

    private final OperationsContainer container;
    private final RuntimeExtra runtimeExtra;
    @Nullable
    private final MessageChannelContext context;
    private final Map<Class<? extends ComponentLoader<?>>, Consumer<ComponentContext>> componentInteractionOverrides;
    private final Map<Class<? extends ModalHandler>, Consumer<ModalContext>> modalInteractionOverrides;

    protected ResponseImpl(@NotNull ResponseBuilder builder) {
        this.map = builder.getMap();
        this.action = builder.getAction();
        this.data = (action != null) ? action.getData() : null;
        this.responseChain = builder.getResponseChain();
        this.runtimeExtra = builder.getRuntimeExtra();
        this.container = builder.getOperationsContainer();
        this.context = map.getContext();
        this.componentInteractionOverrides = builder.getComponentInteractionOverrides();
        this.modalInteractionOverrides = builder.getModalInteractionOverrides();
    }

    @Override
    public void send() {
        if (action == null || data == null || action.isEmpty()) {
            logger.warn("Action or ActionData is null, cannot send response.");
            return;
        }

        IReplyCallback replyCallback = getReplyCallback();
        Message message = getMessage();
        MessageChannel channel = getMessageChannel();
        boolean bothNull = message == null && channel == null;

        // If replyCallBack is present, operation is "send" and channel or message haven't been overridden.
        if (replyCallback != null && isSend() && !action.isChannelOrMessageProvided()) {
            if (replyCallback.isAcknowledged()) {
                performInteractionEditAction(replyCallback);
                return;
            }
            performInteractionAction(replyCallback);
            return;
        }

        if (action.getModal() != null) {
            performModalAction();
            return;
        }

        if (bothNull) {
            throw new IllegalStateException("Either message, channel or modal must be provided in ResponseData.");
        }

        if (data.isSendTyping()) {
            sendTyping((message != null) ? message.getChannel() : channel);
            return;
        }

        if (message != null) {
            performMessageAction(message);
        } else {
            performChannelAction(channel);
        }
    }

    // Action performs

    private void performInteractionAction(IReplyCallback replyCallback) {
        ReplyCallbackAction action = getReplyCallbackAction(replyCallback);
        action.queueAfter(
                (long) data.getDelayMillis(),
                TimeUnit.MILLISECONDS,
                interactionHook -> interactionHook.retrieveOriginal().queue(sentMessage -> {
                    setupRuntimeComponentsTimeout(sentMessage);
                    handleNextResponse(sentMessage);
                    container.onMessageSend(sentMessage);
                }),
                throwable -> {
                    boolean set = container.onExceptionIfSet(throwable);
                    if (!set) {
                        logger.error("Failed to send response in interaction context.", throwable);
                    }
                }
        );
    }

    private void performInteractionEditAction(IReplyCallback replyCallback) {
        WebhookMessageEditAction<Message> action = getWebhookEditAction(replyCallback);
        action.queueAfter(
                (long) data.getDelayMillis(),
                TimeUnit.MILLISECONDS,
                editedMessage -> {
                    setupRuntimeComponentsTimeout(editedMessage);
                    handleNextResponse(editedMessage);
                    container.onMessageSend(editedMessage); // This is still a send operation.
                },
                throwable -> {
                    boolean set = container.onExceptionIfSet(throwable);
                    if (!set) {
                        logger.error("Failed to edit message in interaction context.", throwable);
                    }
                }
        );
    }

    private void performModalAction() {
        IModalCallback modalCallback;
        if (context instanceof SlashCommandContext slashContext) {
            modalCallback = slashContext.getInteraction();
        } else if (context instanceof ComponentContext componentContext) {
            modalCallback = componentContext.getComponentInteraction();
        } else {
            throw new IllegalStateException("Modal can only be sent in a slash command or component context.");
        }

        Modal modal = registerRuntimeModal();
        modalCallback.replyModal(modal).queueAfter(
                (long) data.getDelayMillis(),
                TimeUnit.MILLISECONDS,
                x -> container.onModalSend(),
                throwable -> {
                    boolean set = container.onExceptionIfSet(throwable);
                    if (!set) {
                        logger.error("Failed to send modal in interaction context.", throwable);
                    }
                }
        );
    }

    private void performMessageAction(Message message) {
        List<String> reactions = data.getReactions();
        if (data.getThreadName() != null) {
            createThread(message);
        } else if (reactions != null && !reactions.isEmpty()) {
            addReactions(message);
        } else if (data.isEdit()) {
            editMessage(message);
        } else if (data.isDelete()) {
            deleteMessage(message);
        } else if (data.isReply()) {
            replyToMessage(message);
        } else if (data.isPin()) {
            pinMessage(message);
        } else {
            performChannelAction(message.getChannel());
        }
    }

    private void performChannelAction(MessageChannel channel) {
        MessageCreateAction action = getMessageCreateAction(channel);
        action.queueAfter(
                (long) data.getDelayMillis(),
                TimeUnit.MILLISECONDS,
                sentMessage -> {
                    setupRuntimeComponentsTimeout(sentMessage);
                    handleNextResponse(sentMessage);
                    container.onMessageSend(sentMessage);
                },
                throwable -> {
                    boolean set = container.onExceptionIfSet(throwable);
                    if (!set) {
                        logger.error("Failed to send response in channel context.", throwable);
                    }
                }
        );
    }

    // Message actions

    private void editMessage(Message message) {
        MessageEditAction action = getMessageEditAction(message);
        action.queueAfter(
                (long) data.getDelayMillis(),
                TimeUnit.MILLISECONDS,
                editedMessage -> {
                    setupRuntimeComponentsTimeout(editedMessage);
                    handleNextResponse(editedMessage);
                    container.onMessageEdit(editedMessage);
                },
                throwable -> {
                    boolean set = container.onExceptionIfSet(throwable);
                    if (!set) {
                        logger.error("Failed to edit message in channel context.", throwable);
                    }
                }
        );
    }

    private void deleteMessage(Message message) {
        message.delete().queueAfter(
                (long) data.getDelayMillis(),
                TimeUnit.MILLISECONDS,
                x -> {
                    container.onMessageDelete();
                    handleNextResponse(message);
                },
                throwable -> {
                    boolean set = container.onExceptionIfSet(throwable);
                    if (!set) {
                        logger.error("Failed to delete message in channel context.", throwable);
                    }
                }
        );
    }

    private void replyToMessage(Message message) {
        MessageCreateAction action = getMessageCreateAction(message.getChannel())
                .setMessageReference(message);
        action.queueAfter(
                (long) data.getDelayMillis(),
                TimeUnit.MILLISECONDS,
                sentMessage -> {
                    setupRuntimeComponentsTimeout(sentMessage);
                    handleNextResponse(sentMessage);
                    container.onMessageSend(sentMessage);
                },
                throwable -> {
                    boolean set = container.onExceptionIfSet(throwable);
                    if (!set) {
                        logger.error("Failed to reply to message in channel context.", throwable);
                    }
                }
        );
    }

    private void pinMessage(Message message) {
        message.pin().queueAfter(
                (long) data.getDelayMillis(),
                TimeUnit.MILLISECONDS,
                x -> {
                    container.onMessagePin(message);
                    handleNextResponse(message);
                },
                throwable -> {
                    boolean set = container.onExceptionIfSet(throwable);
                    if (!set) {
                        logger.error("Failed to pin message in channel context.", throwable);
                    }
                }
        );
    }

    // Miscellaneous actions

    private void createThread(Message message) {
        String threadName = map.applyPlaceholders(data.getThreadName());
        message.createThreadChannel(threadName).queueAfter(
                (long) data.getDelayMillis(),
                TimeUnit.MILLISECONDS,
                threadChannel -> {
                    container.onThreadCreate(threadChannel);
                    handleNextResponse(message);
                },
                throwable -> {
                    boolean set = container.onExceptionIfSet(throwable);
                    if (!set) {
                        logger.error("Failed to create thread in channel context.", throwable);
                    }
                }
        );
    }

    private void addReactions(Message message) {
        List<String> reactions = data.getReactions();
        if (reactions == null || reactions.isEmpty()) {
            return;
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                boolean firstTime = true;
                for (String rawReaction : reactions) {
                    Emoji emoji = Emoji.fromFormatted(map.applyPlaceholders(rawReaction));
                    if (firstTime) {
                        message.addReaction(emoji).queue(x -> handleNextResponse(message));
                        firstTime = false;
                        continue;
                    }
                    message.addReaction(emoji).queue();
                }
            }
        }, (long) data.getDelayMillis());
    }

    private void sendTyping(MessageChannel channel) {
        channel.sendTyping().queueAfter(
                (long) data.getDelayMillis(),
                TimeUnit.MILLISECONDS,
                x -> handleNextResponse(null),
                throwable -> {
                    boolean set = container.onExceptionIfSet(throwable);
                    if (!set) {
                        logger.error("Failed to send typing indicator in channel context.", throwable);
                    }
                }
        );
    }

    // Action creation

    private MessageCreateAction getMessageCreateAction(MessageChannel messageChannel) {
        String content = getContent();
        List<MessageEmbed> embeds = getEmbeds();
        List<FileUpload> files = getFileUploads();
        List<LayoutComponent> actionRows = registerRuntimeComponentsAndSortRows();

        return messageChannel.sendMessage(content)
                .setEmbeds(embeds)
                .setFiles(files)
                .setComponents(actionRows);
    }

    private ReplyCallbackAction getReplyCallbackAction(IReplyCallback replyCallback) {
        String content = getContent();
        List<MessageEmbed> embeds = getEmbeds();
        List<FileUpload> files = getFileUploads();
        List<LayoutComponent> actionRows = registerRuntimeComponentsAndSortRows();

        return replyCallback.reply(content)
                .setEmbeds(embeds)
                .setFiles(files)
                .setComponents(actionRows)
                .setEphemeral(data.isEphemeral());
    }

    private WebhookMessageEditAction<Message> getWebhookEditAction(IReplyCallback replyCallback) {
        String content = getContent();
        List<MessageEmbed> embeds = getEmbeds();
        List<FileUpload> files = getFileUploads();
        List<LayoutComponent> actionRows = registerRuntimeComponentsAndSortRows();

        return replyCallback.getHook().editOriginal(content)
                .setEmbeds(embeds)
                .setFiles(files)
                .setComponents(actionRows);
    }

    private MessageEditAction getMessageEditAction(Message message) {
        String content = getContent();
        List<MessageEmbed> embeds = getEmbeds();
        List<FileUpload> files = getFileUploads();
        List<LayoutComponent> actionRows = registerRuntimeComponentsAndSortRows();

        if (content.isEmpty()) {
            if (data.isClearContent()) {
                content = "";
            } else {
                content = message.getContentRaw();
            }
        }

        if (embeds.isEmpty() && !data.isClearEmbeds()) {
            embeds = message.getEmbeds();
        }

        if (files.isEmpty() && !data.isClearFiles()) {
            files = message.getAttachments().stream()
                    .map(attachment -> FileUpload.fromData(new File(attachment.getUrl())))
                    .toList();
        }

        if (actionRows.isEmpty() && !data.isClearComponents()) {
            actionRows = message.getComponents();
        }

        if (data.isClearReactions()) {
            message.clearReactions().queue();
        }

        return message.editMessage(content)
                .setEmbeds(embeds)
                .setFiles(files)
                .setComponents(actionRows);
    }

    // Runtime elements

    private List<LayoutComponent> registerRuntimeComponentsAndSortRows() {
        User user = getUser();
        if (user == null) {
            return List.of();
        }

        // ActionComponent: Row index (0-4)
        Map<ActionComponent, Integer> indexes = new HashMap<>();
        List<Pair<ComponentHandler<?>, Object>> components = action.getComponents();
        if (components == null || components.isEmpty()) {
            return Collections.emptyList();
        }

        for (Pair<ComponentHandler<?>, Object> pair : components) {
            ComponentHandler<?> handler = pair.getFirst();
            Object dataObject = pair.getSecond();
            RowComponent rowComponent = handler.getRowComponent(map, dataObject);
            ActionComponent actionComponent = rowComponent.getActionComponent();

            RuntimeComponent runtimeComponent = new RuntimeComponentImpl(
                    user,
                    handler,
                    (ForgedComponentDataContainer) dataObject,
                    actionComponent,
                    runtimeExtra,
                    componentInteractionOverrides.getOrDefault(handler.getClass(), null)
            );

            componentManager.addRuntimeComponent(runtimeComponent);
            indexes.put(actionComponent, rowComponent.getRowIndex());
        }

        List<LayoutComponent> rows = new ArrayList<>();
        for (int i = 0; i < 5; i++) { // 5 is the maximum number of rows
            List<ActionComponent> componentsInRow = new ArrayList<>();
            for (Map.Entry<ActionComponent, Integer> entry : indexes.entrySet()) {
                if (entry.getValue() == i) {
                    componentsInRow.add(entry.getKey());
                }
            }
            if (!componentsInRow.isEmpty()) {
                rows.add(ActionRow.of(componentsInRow));
            }
        }

        return rows;
    }

    private Modal registerRuntimeModal() {
        Pair<? extends ModalHandler, ForgedModalData> modalPair = action.getModal();
        if (modalPair == null) {
            throw new IllegalStateException("Modal must be set in ResponseData.");
        }
        ModalHandler modalHandler = modalPair.getFirst();
        ForgedModalData modalData = modalPair.getSecond();

        User user = getUser();
        if (user == null) {
            throw new IllegalStateException("User not present in ResponseData.");
        }
        String uniqueId = Utils.generateUniqueComponentId(modalHandler.getId());
        Modal modal = modalData.getModal(uniqueId, map);

        RuntimeModal runtimeModal = new RuntimeModalImpl(
                user,
                modalHandler,
                modalData,
                modal,
                runtimeExtra,
                modalInteractionOverrides.getOrDefault(modalHandler.getClass(), null)
        );

        modalManager.addRuntimeModal(runtimeModal);
        return modal;
    }

    private void setupRuntimeComponentsTimeout(@NotNull Message message) {
        for (LayoutComponent layout : message.getComponents()) {
            for (ActionComponent action : layout.getActionComponents()) {
                String id = action.getId();
                if (id == null || id.isEmpty()) {
                    continue;
                }
                setupRuntimeComponentTimeout(
                        componentManager.getRuntimeComponent(id),
                        message
                );
            }
        }
    }

    private void setupRuntimeComponentTimeout(@Nullable RuntimeComponent runtimeComponent, @NotNull Message message) {
        if (runtimeComponent == null) {
            return;
        }
        runtimeComponent.setupTimeout(message);
    }

    // Getter methods

    @Nullable
    private IReplyCallback getReplyCallback() {
        if (context instanceof ReplyCallbackContext replyCallbackContext) {
            return replyCallbackContext.getReplyCallback();
        }
        return null;
    }

    @Nullable
    private User getUser() {
        return (context == null) ? null : context.getUser();
    }

    @Nullable
    private Message getMessage() {
        return action.getMessage();
    }

    @Nullable
    private MessageChannel getMessageChannel() {
        return action.getMessageChannel();
    }

    private boolean isSend() {
        List<String> reactions = data.getReactions();
        return !(data.isEdit()
                || data.isReply()
                || data.isDelete()
                || data.isSendTyping()
                || data.getThreadName() != null
                || action.getModal() != null
                || data.isPin()
                || (reactions != null && !reactions.isEmpty())
        );
    }

    private String getContent() {
        String content = data.getContent();
        if (content == null || content.isEmpty()) {
            return "";
        }
        return map.applyPlaceholders(content);
    }

    private List<MessageEmbed> getEmbeds() {
        List<MessageEmbed> embedBuilders = new ArrayList<>();
        if (data.getEmbeds() == null) {
            return embedBuilders;
        }
        for (MessageEmbedData embedData : data.getEmbeds()) {
            EmbedBuilder builder = embedData.getEmbedBuilder(map, instance.getConfig().getTruncationIndicator());
            if (builder.isEmpty()) {
                continue;
            }
            embedBuilders.add(builder.build());
        }
        return embedBuilders;
    }

    private List<FileUpload> getFileUploads() {
        List<FileUpload> fileUploads = new ArrayList<>();
        if (data.getFilePaths() == null || data.getFilePaths().isEmpty()) {
            return fileUploads;
        }
        for (File file : action.getFiles()) {
            fileUploads.add(FileUpload.fromData(file));
        }
        return fileUploads;
    }

    private void handleNextResponse(@Nullable Message message) {
        if (responseChain.isEmpty()) {
            return;
        }
        Action action = responseChain.getFirst();
        if (action.getData().isInheritMessage() && message != null) {
            action.setMessage(message);
            action.setChannelOrMessageProvided(true);
        }
        if (message != null) {
            DiscordCache.cacheMessage(message); // Cache message for faster retrieval in case of later use.
        }
        ResponseBuilder.create(map, responseChain).buildAndSend();
    }

}

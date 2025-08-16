package me.mrepiko.cymric.response;

import lombok.Getter;
import me.mrepiko.cymric.CymricApi;
import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.context.components.ComponentContext;
import me.mrepiko.cymric.context.modal.ModalContext;
import me.mrepiko.cymric.elements.components.ComponentLoader;
import me.mrepiko.cymric.elements.components.selectmenus.stringselect.GenericStringSelectMenu;
import me.mrepiko.cymric.elements.components.selectmenus.stringselect.data.StringSelectMenuOptionData;
import me.mrepiko.cymric.elements.modal.ModalTemplate;
import me.mrepiko.cymric.jackson.JsonContainer;
import me.mrepiko.cymric.managers.runtime.RuntimeExtra;
import me.mrepiko.cymric.mics.Utils;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.response.data.ActionData;
import me.mrepiko.cymric.response.data.ResponseData;
import me.mrepiko.cymric.response.data.embed.FieldData;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@Getter
public class ResponseBuilder {

    private final CymricApi instance = DiscordBot.getInstance();

    private final PlaceholderMap map;
    @Nullable
    private Action action;
    @Nullable
    private ResponseChain responseChain;

    private final OperationsContainer operationsContainer = new OperationsContainer();
    private final RuntimeExtra runtimeExtra = new RuntimeExtra();

    private final Map<Class<? extends ComponentLoader<?>>, Consumer<ComponentContext>> componentInteractionOverrides = new HashMap<>();
    private final Map<Class<? extends ModalTemplate>, Consumer<ModalContext>> modalInteractionOverrides = new HashMap<>();

    private ResponseBuilder(PlaceholderMap map, @Nullable Action action, @Nullable ResponseChain responseChain) {
        this.map = map;
        this.action = action;
        this.responseChain = responseChain;
    }

    public static ResponseBuilder create(@NotNull PlaceholderMap map, @NotNull ActionData actionData) {
        return create(map, new ResponseData(actionData));
    }

    public static ResponseBuilder create(@NotNull PlaceholderMap map, @NotNull ResponseData responseData) {
        ResponseChain responseChain = new ResponseChain();
        responseChain.addAll(responseData.stream().map(Action::new).toList());
        return create(map, responseChain);
    }

    public static ResponseBuilder create(@NotNull PlaceholderMap map, @NotNull JsonContainer responseContainer) {
        return create(map, responseContainer.getAs(ResponseData.class));
    }

    protected static ResponseBuilder create(@NotNull PlaceholderMap map, @NotNull ResponseChain chain) {
        Action first = chain.poll();
        if (first == null) {
            return new ResponseBuilder(map, null, null);
        }

        CompletableFuture<Void> future = CompletableFuture
                .runAsync(() -> first.initialize(map))
                .exceptionally(throwable -> {
                    DiscordBot.getLogger().error("Failed to initialize action: {}", first, throwable);
                    return null;
                })
                .orTimeout(5000, TimeUnit.MILLISECONDS);
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return new ResponseBuilder(map, first, chain);
    }

    public ResponseBuilder setContainer(@NotNull JsonContainer container) {
        this.responseChain = container.getAs(ResponseChain.class);
        this.action = this.responseChain.getFirst();
        return this;
    }

    public ResponseBuilder overrideComponentInteraction(@NotNull Class<? extends ComponentLoader<?>> clazz, @NotNull Consumer<ComponentContext> consumer) {
        this.componentInteractionOverrides.put(clazz, consumer);
        return this;
    }

    public ResponseBuilder overrideModalInteraction(@NotNull Class<? extends ModalTemplate> clazz, @NotNull Consumer<ModalContext> consumer) {
        this.modalInteractionOverrides.put(clazz, consumer);
        return this;
    }

    public ResponseBuilder setAction(@NotNull Consumer<Action> consumer) {
        return setAction(0, consumer);
    }

    public ResponseBuilder setAction(int index, @NotNull Consumer<Action> consumer) {
        if (index == 0) {
            if (this.action == null) {
                throw new IllegalStateException("Action is not set. Cannot set action data at index 0.");
            }
            consumer.accept(this.action);
        } else {
            if (this.responseChain == null) {
                throw new IllegalStateException("Response chain is not set. Cannot set action data at index " + index);
            }
            if (index < 0 || index >= this.responseChain.size()) {
                throw new IndexOutOfBoundsException("Index out of bounds: " + index);
            }
            consumer.accept(this.responseChain.get(index));
        }
        return this;
    }

    public ResponseBuilder setExtra(@NotNull Consumer<RuntimeExtra> consumer) {
        consumer.accept(this.runtimeExtra);
        return this;
    }

    public ResponseBuilder onMessageSend(@NotNull Consumer<Message> consumer) {
        this.operationsContainer.setOnMessageSend(consumer);
        return this;
    }

    public ResponseBuilder onMessageEdit(@NotNull Consumer<Message> consumer) {
        this.operationsContainer.setOnMessageEdit(consumer);
        return this;
    }

    public ResponseBuilder onMessageDelete(@NotNull Consumer<Void> consumer) {
        this.operationsContainer.setOnMessageDelete(consumer);
        return this;
    }

    public ResponseBuilder onThreadCreate(@NotNull Consumer<ThreadChannel> consumer) {
        this.operationsContainer.setOnThreadCreate(consumer);
        return this;
    }

    public ResponseBuilder onMessagePin(@NotNull Consumer<Message> consumer) {
        this.operationsContainer.setOnMessagePin(consumer);
        return this;
    }

    public ResponseBuilder onModalSend(@NotNull Consumer<Void> consumer) {
        this.operationsContainer.setOnModalSend(consumer);
        return this;
    }

    public ResponseBuilder onException(@NotNull Consumer<Throwable> consumer) {
        this.operationsContainer.setOnException(consumer);
        return this;
    }

    public ResponseBuilder setExtra(@NotNull RuntimeExtra runtimeExtra) {
        this.runtimeExtra.clear();
        this.runtimeExtra.putAll(runtimeExtra);
        return this;
    }

    public ResponseBuilder injectStringSelectMenuOptions(@NotNull Class<? extends GenericStringSelectMenu> clazz, @NotNull List<StringSelectMenuOptionData> options) {
        if (this.action != null) {
            this.action.injectStringSelectMenuOptions(clazz, options);
        }
        return this;
    }

    public ResponseBuilder injectEmbedFields(int embedIndex, @NotNull List<FieldData> fieldsData) {
        if (this.action != null) {
            this.action.injectEmbedFields(embedIndex, fieldsData);
        }
        return this;
    }

    public Response build() {
        if (action == null) {
            return new ResponseImpl(this);
        }

        Message message = action.getMessage();
        if (action.getData().isEdit() && message != null) {
            List<MessageEmbed> embeds = message.getEmbeds();
            if (embeds.size() == 1) {
                map.put("old_embed", embeds.getFirst()); // Most of the time, there will be only one embed.
            } else {
                for (int i = 0; i < embeds.size(); i++) {
                    map.put("old_embed_" + i, embeds.get(i));
                }
            }
        }

        if (responseChain != null && !responseChain.isEmpty()) {
            Action nextAction = responseChain.getFirst();
            double delayMillis = nextAction.getData().getDelayMillis() + action.getData().getDelayMillis();
            delayMillis += 500; // To match up with latency and other factors.
            int delaySeconds = (int) delayMillis / 1000;
            map.put("next_action_execution_timestamp", Utils.getCurrentTimeSeconds() + delaySeconds);
            map.put("next_action_execution_timestamp_millis", System.currentTimeMillis() + delayMillis);
            map.put("next_action_execution_delay", delayMillis);
            map.put("next_action_execution_delay_seconds", delaySeconds);
        }

        return new ResponseImpl(this);
    }

    public void buildAndSend() {
        Response response = build();
        response.send();
    }

}

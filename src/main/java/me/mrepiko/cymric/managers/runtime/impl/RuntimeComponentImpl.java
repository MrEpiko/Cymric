package me.mrepiko.cymric.managers.runtime.impl;

import lombok.Getter;
import lombok.Setter;
import me.mrepiko.cymric.context.components.ComponentContext;
import me.mrepiko.cymric.context.plain.impl.MessageContextImpl;
import me.mrepiko.cymric.elements.components.ComponentHandler;
import me.mrepiko.cymric.elements.components.ForgedComponentDataContainer;
import me.mrepiko.cymric.managers.runtime.RuntimeComponent;
import me.mrepiko.cymric.managers.runtime.RuntimeExtra;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

@Getter
public class RuntimeComponentImpl implements RuntimeComponent {

    private final ActionComponent actionComponent;
    private final ComponentHandler<?> element;
    private final ForgedComponentDataContainer overriddenData;
    private final RuntimeExtra extra = new RuntimeExtra();

    @Nullable
    private final Consumer<ComponentContext> interactionOverride;

    @Setter
    private boolean enabled = true;
    private Message message;
    private final User creator;

    // Also used for identifying the RuntimeComponent.
    private final String uniqueElementId;

    public RuntimeComponentImpl(
            @NotNull User creator,
            @NotNull ComponentHandler<?> element,
            @Nullable ForgedComponentDataContainer overriddenData,
            @NotNull ActionComponent actionComponent,
            @NotNull RuntimeExtra extra,
            @Nullable Consumer<ComponentContext> interactionOverride
    ) {
        this.creator = creator;
        this.uniqueElementId = actionComponent.getId();
        this.element = element;
        this.overriddenData = overriddenData;
        this.actionComponent = actionComponent;
        this.interactionOverride = interactionOverride;
        this.extra.putAll(extra);
    }

    @NotNull
    @Override
    public ActionComponent getActionComponent() {
        return actionComponent;
    }

    @Override
    @NotNull
    public ForgedComponentDataContainer getOverriddenData() {
        return overriddenData != null ? overriddenData : element.getData();
    }

    @Override
    public void setupTimeout(Message message) {
        this.message = message;
        long timeoutMillis = (overriddenData == null)
                ? (long) element.getData().getTimeoutableElementData().getTimeoutMillis()
                : (long) overriddenData.getTimeoutableElementData().getTimeoutMillis();

        if (timeoutMillis <= 0) {
            return;
        }

        RuntimeComponent runtimeComponent = this;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                element.onTimeout(runtimeComponent, new MessageContextImpl(message));
            }
        }, timeoutMillis);
    }

}

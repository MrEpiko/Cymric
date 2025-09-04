package me.mrepiko.cymric.managers.runtime;

import me.mrepiko.cymric.context.components.ComponentContext;
import me.mrepiko.cymric.elements.components.ComponentHandler;
import me.mrepiko.cymric.elements.components.ComponentLoader;
import me.mrepiko.cymric.elements.components.ForgedComponentDataContainer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import org.jetbrains.annotations.NotNull;

public interface RuntimeComponent extends RuntimeElement<ComponentHandler<?>, ForgedComponentDataContainer, ComponentContext> {
    @NotNull
    ActionComponent getActionComponent();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    void setupTimeout(Message message);
}

package me.mrepiko.cymric.elements.components;

import me.mrepiko.cymric.context.plain.MessageContext;
import me.mrepiko.cymric.elements.plain.Conditionable;
import me.mrepiko.cymric.elements.plain.SerializableBotElement;
import me.mrepiko.cymric.elements.components.managers.runtime.RuntimeComponent;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import org.jetbrains.annotations.NotNull;

public interface ComponentHandler<T extends ForgedComponentDataContainer> extends SerializableBotElement<T>, Conditionable {
    void onTimeout(@NotNull RuntimeComponent runtimeComponent, @NotNull MessageContext context);

    @NotNull
    RowComponent getRowComponent(@NotNull PlaceholderMap map, @NotNull Object overriddenData);
}

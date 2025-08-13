package me.mrepiko.cymric.elements.plain;

import me.mrepiko.cymric.context.plain.MessageContext;
import me.mrepiko.cymric.elements.components.RowComponent;
import me.mrepiko.cymric.managers.runtime.RuntimeComponent;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import org.jetbrains.annotations.NotNull;

public interface ComponentTemplate {
    void onTimeout(@NotNull RuntimeComponent runtimeComponent, @NotNull MessageContext context);

    @NotNull
    RowComponent getRowComponent(@NotNull PlaceholderMap map, @NotNull Object overriddenData);
}

package me.mrepiko.cymric.elements.components.button;

import me.mrepiko.cymric.context.components.ButtonContext;
import me.mrepiko.cymric.elements.components.ComponentHandler;
import me.mrepiko.cymric.elements.components.button.data.ForgedButtonData;
import org.jetbrains.annotations.NotNull;

public interface ButtonHandler extends ComponentHandler<ForgedButtonData> {
    void onInteraction(@NotNull ButtonContext context);
}

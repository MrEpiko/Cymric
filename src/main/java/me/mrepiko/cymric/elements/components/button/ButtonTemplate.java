package me.mrepiko.cymric.elements.components.button;

import me.mrepiko.cymric.context.components.ButtonContext;
import org.jetbrains.annotations.NotNull;

public interface ButtonTemplate {
    void onInteraction(@NotNull ButtonContext context);
}

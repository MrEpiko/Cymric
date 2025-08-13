package me.mrepiko.cymric.elements.components.selectmenus.entityselect;

import me.mrepiko.cymric.context.components.EntitySelectMenuContext;
import org.jetbrains.annotations.NotNull;

public interface EntitySelectMenuTemplate {
    void onInteraction(@NotNull EntitySelectMenuContext context);
}

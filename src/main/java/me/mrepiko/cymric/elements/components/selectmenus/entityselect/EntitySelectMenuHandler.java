package me.mrepiko.cymric.elements.components.selectmenus.entityselect;

import me.mrepiko.cymric.context.components.EntitySelectMenuContext;
import me.mrepiko.cymric.elements.components.ComponentHandler;
import me.mrepiko.cymric.elements.components.selectmenus.entityselect.data.ForgedEntitySelectMenuData;
import me.mrepiko.cymric.elements.containers.ElementDataContainer;
import me.mrepiko.cymric.elements.plain.SerializableBotElement;
import org.jetbrains.annotations.NotNull;

public interface EntitySelectMenuHandler extends ComponentHandler<ForgedEntitySelectMenuData> {
    void onInteraction(@NotNull EntitySelectMenuContext context);
}

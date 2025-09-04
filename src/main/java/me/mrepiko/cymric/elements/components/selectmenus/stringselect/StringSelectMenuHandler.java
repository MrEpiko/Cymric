package me.mrepiko.cymric.elements.components.selectmenus.stringselect;

import me.mrepiko.cymric.context.components.StringSelectMenuContext;
import me.mrepiko.cymric.elements.components.ComponentHandler;
import me.mrepiko.cymric.elements.components.selectmenus.stringselect.data.ForgedStringSelectMenuData;
import me.mrepiko.cymric.elements.containers.ElementDataContainer;
import me.mrepiko.cymric.elements.plain.SerializableBotElement;
import org.jetbrains.annotations.NotNull;

public interface StringSelectMenuHandler extends ComponentHandler<ForgedStringSelectMenuData> {
    void onInteraction(@NotNull StringSelectMenuContext context);
}

package me.mrepiko.cymric.context.components;

import me.mrepiko.cymric.elements.components.selectmenus.stringselect.StringSelectMenuHandler;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface StringSelectMenuContext extends ComponentContext {
    @NotNull
    StringSelectInteractionEvent getEvent();

    @NotNull
    StringSelectInteraction getStringSelectInteraction();

    @NotNull
    StringSelectMenuHandler getStringSelectMenuHandler();

    @NotNull
    StringSelectMenu getStringSelectMenu();

    @NotNull
    List<String> getSelectedValues();
}

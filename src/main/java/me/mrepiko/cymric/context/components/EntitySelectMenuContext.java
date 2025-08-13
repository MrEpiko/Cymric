package me.mrepiko.cymric.context.components;

import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectInteraction;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface EntitySelectMenuContext extends ComponentContext {
    @NotNull
    EntitySelectInteractionEvent getEvent();

    @NotNull
    EntitySelectInteraction getInteraction();

    @NotNull
    EntitySelectMenu getEntitySelectMenu();

    @NotNull
    List<IMentionable> getSelectedValues();
}

package me.mrepiko.cymric.context.components;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import org.jetbrains.annotations.NotNull;

public interface ButtonContext extends ComponentContext {
    @NotNull
    ButtonInteractionEvent getEvent();

    @NotNull
    ButtonInteraction getButtonInteraction();

    @NotNull
    Button getButton();
}

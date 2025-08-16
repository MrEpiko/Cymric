package me.mrepiko.cymric.context.components.impl;

import lombok.AllArgsConstructor;
import me.mrepiko.cymric.elements.components.ComponentLoader;
import me.mrepiko.cymric.managers.runtime.RuntimeComponent;
import me.mrepiko.cymric.context.components.EntitySelectMenuContext;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectInteraction;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AllArgsConstructor
public class EntitySelectMenuContextImpl implements EntitySelectMenuContext {

    private final EntitySelectInteractionEvent event;
    private final RuntimeComponent runtimeComponent;

    @NotNull
    @Override
    public EntitySelectInteractionEvent getEvent() {
        return event;
    }

    @NotNull
    @Override
    public EntitySelectInteraction getInteraction() {
        return event.getInteraction();
    }

    @NotNull
    @Override
    public EntitySelectMenu getEntitySelectMenu() {
        return event.getSelectMenu();
    }

    @NotNull
    @Override
    public List<IMentionable> getSelectedValues() {
        return event.getValues();
    }

    @NotNull
    @Override
    public RuntimeComponent getRuntimeComponent() {
        return runtimeComponent;
    }

    @NotNull
    @Override
    public ComponentInteraction getComponentInteraction() {
        return getInteraction();
    }

    @NotNull
    @Override
    public ComponentLoader<?> getComponentHolder() {
        return runtimeComponent.getElement();
    }

    @NotNull
    @Override
    public Message getMessage() {
        return event.getMessage();
    }

    @NotNull
    @Override
    public MessageChannel getMessageChannel() {
        return event.getChannel();
    }

    @Nullable
    @Override
    public Guild getGuild() {
        return event.getGuild();
    }

    @NotNull
    @Override
    public User getUser() {
        return event.getUser();
    }

    @Nullable
    @Override
    public Member getMember() {
        return event.getMember();
    }

}

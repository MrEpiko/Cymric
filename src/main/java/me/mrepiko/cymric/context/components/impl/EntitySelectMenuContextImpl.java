package me.mrepiko.cymric.context.components.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.mrepiko.cymric.context.components.EntitySelectMenuContext;
import me.mrepiko.cymric.elements.components.ComponentHandler;
import me.mrepiko.cymric.elements.components.selectmenus.entityselect.EntitySelectMenuHandler;
import me.mrepiko.cymric.elements.components.managers.runtime.RuntimeComponent;
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
@Getter
public class EntitySelectMenuContextImpl implements EntitySelectMenuContext {

    private final EntitySelectInteractionEvent event;
    private final EntitySelectMenuHandler entitySelectMenuHandler;
    private final RuntimeComponent runtimeComponent;

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
    public ComponentInteraction getComponentInteraction() {
        return getInteraction();
    }

    @Override
    public @NotNull ComponentHandler<?> getComponentHandler() {
        return entitySelectMenuHandler;
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

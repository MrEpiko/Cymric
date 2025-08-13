package me.mrepiko.cymric.context.components.impl;

import lombok.AllArgsConstructor;
import me.mrepiko.cymric.elements.components.ComponentHolder;
import me.mrepiko.cymric.managers.runtime.RuntimeComponent;
import me.mrepiko.cymric.context.components.StringSelectMenuContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AllArgsConstructor
public class StringSelectMenuContextImpl implements StringSelectMenuContext {

    private final StringSelectInteractionEvent event;
    private final RuntimeComponent runtimeComponent;

    @NotNull
    @Override
    public StringSelectInteractionEvent getEvent() {
        return event;
    }

    @NotNull
    @Override
    public StringSelectInteraction getStringSelectInteraction() {
        return event.getInteraction();
    }

    @NotNull
    @Override
    public StringSelectMenu getStringSelectMenu() {
        return event.getSelectMenu();
    }

    @NotNull
    @Override
    public List<String> getSelectedValues() {
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
        return getStringSelectInteraction();
    }

    @NotNull
    @Override
    public ComponentHolder<?> getComponentHolder() {
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

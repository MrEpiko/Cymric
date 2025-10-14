package me.mrepiko.cymric.context.components.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.mrepiko.cymric.context.components.ButtonContext;
import me.mrepiko.cymric.elements.components.ComponentHandler;
import me.mrepiko.cymric.elements.components.button.ButtonHandler;
import me.mrepiko.cymric.elements.components.managers.runtime.RuntimeComponent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Getter
public class ButtonContextImpl implements ButtonContext {

    private final ButtonInteractionEvent event;
    private final ButtonHandler buttonHandler;
    private final RuntimeComponent runtimeComponent;

    @NotNull
    @Override
    public ButtonInteraction getButtonInteraction() {
        return event.getInteraction();
    }

    @NotNull
    @Override
    public Button getButton() {
        return event.getButton();
    }

    @NotNull
    @Override
    public ComponentInteraction getComponentInteraction() {
        return getButtonInteraction();
    }

    @Override
    public @NotNull ComponentHandler<?> getComponentHandler() {
        return buttonHandler;
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

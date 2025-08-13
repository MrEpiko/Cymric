package me.mrepiko.cymric.context.modal;

import lombok.AllArgsConstructor;
import me.mrepiko.cymric.managers.runtime.RuntimeModal;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public class ModalContextImpl implements ModalContext {

    private final ModalInteractionEvent event;
    private final Modal modal;
    private final RuntimeModal runtimeModal;

    @NotNull
    @Override
    public ModalInteractionEvent getEvent() {
        return event;
    }

    @NotNull
    @Override
    public ModalInteraction getInteraction() {
        return event.getInteraction();
    }

    @NotNull
    @Override
    public Modal getModal() {
        return modal;
    }

    @Nullable
    @Override
    public String getValue(String fieldId) {
        ModalMapping value = event.getValue(fieldId);
        if (value == null) {
            return null;
        }
        return value.getAsString().trim();
    }

    @Nullable
    @Override
    public Message getMessage() {
        return event.getMessage();
    }

    @NotNull
    @Override
    public RuntimeModal getRuntimeModal() {
        return runtimeModal;
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
        return null;
    }
}

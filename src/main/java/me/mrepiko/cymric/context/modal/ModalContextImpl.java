package me.mrepiko.cymric.context.modal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.mrepiko.cymric.elements.modal.ModalHandler;
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
@Getter
public class ModalContextImpl implements ModalContext {

    private final ModalInteractionEvent event;
    private final Modal modal;
    private final ModalHandler modalHandler;
    private final RuntimeModal runtimeModal;

    @NotNull
    @Override
    public ModalInteraction getInteraction() {
        return event.getInteraction();
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

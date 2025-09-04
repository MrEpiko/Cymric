package me.mrepiko.cymric.context.modal;

import me.mrepiko.cymric.context.plain.MessageChannelContext;
import me.mrepiko.cymric.elements.modal.ModalHandler;
import me.mrepiko.cymric.managers.runtime.RuntimeModal;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ModalContext extends MessageChannelContext {
    @Nullable
    Message getMessage();

    @NotNull
    RuntimeModal getRuntimeModal();

    @NotNull
    ModalInteractionEvent getEvent();

    @NotNull
    ModalInteraction getInteraction();

    @NotNull
    ModalHandler getModalHandler();

    @Nullable
    Modal getModal();

    @Nullable
    String getValue(String fieldId);
}

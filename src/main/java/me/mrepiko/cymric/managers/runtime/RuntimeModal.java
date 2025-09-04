package me.mrepiko.cymric.managers.runtime;

import me.mrepiko.cymric.context.modal.ModalContext;
import me.mrepiko.cymric.elements.modal.ModalHandler;
import me.mrepiko.cymric.elements.modal.data.ForgedModalData;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

public interface RuntimeModal extends RuntimeElement<ModalHandler, ForgedModalData, ModalContext> {
    @NotNull
    Modal getModal();
}

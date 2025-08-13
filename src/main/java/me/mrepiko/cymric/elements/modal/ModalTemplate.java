package me.mrepiko.cymric.elements.modal;

import me.mrepiko.cymric.context.modal.ModalContext;
import org.jetbrains.annotations.NotNull;

public interface ModalTemplate {
    void onSubmission(@NotNull ModalContext context);
}

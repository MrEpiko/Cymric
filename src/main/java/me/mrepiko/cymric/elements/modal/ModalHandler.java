package me.mrepiko.cymric.elements.modal;

import me.mrepiko.cymric.context.modal.ModalContext;
import me.mrepiko.cymric.elements.containers.ElementDataContainer;
import me.mrepiko.cymric.elements.modal.data.ForgedModalData;
import me.mrepiko.cymric.elements.plain.Conditionable;
import me.mrepiko.cymric.elements.plain.SerializableBotElement;
import org.jetbrains.annotations.NotNull;

public interface ModalHandler extends SerializableBotElement<ForgedModalData>, Conditionable {
    void onSubmission(@NotNull ModalContext context);
}

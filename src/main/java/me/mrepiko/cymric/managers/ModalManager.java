package me.mrepiko.cymric.managers;

import me.mrepiko.cymric.elements.modal.GenericModal;
import me.mrepiko.cymric.managers.runtime.RuntimeModal;
import org.jetbrains.annotations.NotNull;

public interface ModalManager extends ElementManager<GenericModal> {
    void addRuntimeModal(@NotNull RuntimeModal runtimeModal);
}

package me.mrepiko.cymric.elements.modal.managers;

import me.mrepiko.cymric.elements.managers.ElementManager;
import me.mrepiko.cymric.elements.modal.ModalHandler;
import me.mrepiko.cymric.elements.tasks.managers.runtime.RuntimeModal;
import org.jetbrains.annotations.NotNull;

public interface ModalManager extends ElementManager<ModalHandler> {
    void addRuntimeModal(@NotNull RuntimeModal runtimeModal);
}

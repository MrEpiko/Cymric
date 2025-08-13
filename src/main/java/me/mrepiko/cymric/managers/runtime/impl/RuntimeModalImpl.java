package me.mrepiko.cymric.managers.runtime.impl;

import lombok.Getter;
import lombok.Setter;
import me.mrepiko.cymric.context.modal.ModalContext;
import me.mrepiko.cymric.elements.modal.GenericModal;
import me.mrepiko.cymric.elements.modal.data.ForgedModalData;
import me.mrepiko.cymric.managers.runtime.RuntimeExtra;
import me.mrepiko.cymric.managers.runtime.RuntimeModal;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Getter
public class RuntimeModalImpl implements RuntimeModal {

    private final Modal modal;
    private final GenericModal element;
    private final ForgedModalData overriddenData;
    private final RuntimeExtra extra = new RuntimeExtra();

    @Nullable
    private final Consumer<ModalContext> interactionOverride;

    @Setter
    private Message message;
    private final User creator;

    public RuntimeModalImpl(
            @NotNull User creator,
            @NotNull GenericModal element,
            @Nullable ForgedModalData overriddenData,
            @NotNull Modal modal,
            @NotNull RuntimeExtra extra,
            @Nullable Consumer<ModalContext> interactionOverride
    ) {
        this.creator = creator;
        this.element = element;
        this.overriddenData = overriddenData;
        this.modal = modal;
        this.interactionOverride = interactionOverride;
        this.extra.putAll(extra);
    }

    @NotNull
    @Override
    public String getUniqueElementId() {
        return element.getId();
    }

    @NotNull
    @Override
    public ForgedModalData getOverriddenData() {
        return overriddenData;
    }

    @Nullable
    @Override
    public Message getMessage() {
        return message;
    }
}

package me.mrepiko.cymric.elements.modal.managers;

import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.annotations.elements.CymricModal;
import me.mrepiko.cymric.context.modal.ModalContextImpl;
import me.mrepiko.cymric.elements.modal.ModalHandler;
import me.mrepiko.cymric.elements.modal.data.ForgedModalData;
import me.mrepiko.cymric.elements.managers.GenericElementManager;
import me.mrepiko.cymric.elements.tasks.managers.runtime.RuntimeModal;
import me.mrepiko.cymric.mics.Constants;
import me.mrepiko.cymric.mics.Utils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ModalManagerImpl extends GenericElementManager<ModalHandler> implements ModalManager {

    // Invoker ID: RuntimeModal
    // Each time user opens a modal, their RuntimeModal will be overwritten
    private final Map<String, RuntimeModal> runtimeModals = new HashMap<>();

    public ModalManagerImpl() {
        DiscordBot.getInstance().getShardManager().addEventListener(this);
    }

    @Override
    public void addRuntimeModal(@NotNull RuntimeModal runtimeModal) {
        runtimeModals.put(runtimeModal.getCreator().getId(), runtimeModal);
    }

    @Override
    public void register() {
        setupDirectory(Constants.MODAL_CONFIGURATION_FOLDER_PATH);
        register(CymricModal.class, ModalHandler.class);
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modalId = Utils.getSanitizedComponentId(event.getModalId());

        ModalHandler modalHandler = getById(modalId);
        RuntimeModal runtimeModal = getRuntimeModal(event.getUser());
        ForgedModalData data;
        Modal modal;
        if (runtimeModal == null) {
            data = modalHandler.getData();
            modal = data.getModal(modalId, null);
        } else {
            data = runtimeModal.getOverriddenData();
            modal = runtimeModal.getModal();
        }

        ModalContextImpl context = new ModalContextImpl(event, modal, modalHandler, runtimeModal);
        if (!modalHandler.check(context, data.getConditionalData())) {
            return;
        }

        switch (data.getDeferType()) {
            case ENDURING -> event.deferReply().queue();
            case EPHEMERAL -> event.deferReply(true).queue();
            case EDIT -> event.deferEdit().queue();
        }

        if (runtimeModal != null && runtimeModal.getInteractionOverride() != null) {
            runtimeModal.getInteractionOverride().accept(context);
            return;
        }

        modalHandler.onSubmission(context);
        modalHandler.setUserCooldown(event.getUser(), data.getConditionalData());
    }

    @Nullable
    private RuntimeModal getRuntimeModal(@NotNull User user) {
        return runtimeModals.get(user.getId());
    }

}

package me.mrepiko.cymric.elements.modal;

import lombok.Getter;
import me.mrepiko.cymric.context.modal.ModalContext;
import me.mrepiko.cymric.elements.ConditionalElementLoader;
import me.mrepiko.cymric.elements.ElementError;
import me.mrepiko.cymric.elements.modal.data.ForgedModalData;
import me.mrepiko.cymric.elements.plain.SerializableBotElement;
import me.mrepiko.cymric.mics.Constants;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class GenericModal extends ConditionalElementLoader<ForgedModalData> implements ModalTemplate, SerializableBotElement<ForgedModalData> {

    public GenericModal(@NotNull String id) {
        super(id, Constants.MODAL_CONFIGURATION_FOLDER_PATH);
    }

    @Override
    public void reload() {
        super.reload();
        if (this.configMissing) {
            return;
        }
        super.setConditionalData(data.getConditionalData(), "modal", ElementError.getAllWithout(ElementError.INVALID_ARGS));
    }

    @Override
    public void initializeData() {
        ForgedModalData emptyData = new ForgedModalData();
        this.data = config.getOrSetDefault("properties", ForgedModalData.class, emptyData);
        if (this.data == emptyData) {
            this.configMissing = true;
        }
    }

    @Override
    public abstract void onSubmission(@NotNull ModalContext context);

}

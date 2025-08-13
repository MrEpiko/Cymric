package me.mrepiko.cymric.elements.modal;

import lombok.Getter;
import lombok.experimental.Delegate;
import me.mrepiko.cymric.config.ConfigFile;
import me.mrepiko.cymric.context.modal.ModalContext;
import me.mrepiko.cymric.elements.ConditionalHolder;
import me.mrepiko.cymric.elements.ElementError;
import me.mrepiko.cymric.elements.modal.data.ForgedModalData;
import me.mrepiko.cymric.elements.plain.SerializableBotElement;
import me.mrepiko.cymric.jackson.JsonContainer;
import me.mrepiko.cymric.mics.Constants;
import me.mrepiko.cymric.jackson.JacksonUtils;
import me.mrepiko.cymric.mics.Utils;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class GenericModal extends ConditionalHolder implements ModalTemplate, SerializableBotElement<ForgedModalData> {

    private final String filePath;

    protected final String id;
    @Delegate
    protected JsonContainer config;
    protected ForgedModalData data;

    private boolean configMissing;

    public GenericModal(@NotNull String id) {
        this.id = id;
        this.filePath = Constants.MODAL_CONFIGURATION_FOLDER_PATH + id + ".json";
        if (!Utils.isFileExists(this.filePath)) {
            this.configMissing = true;
        }
    }

    @Override
    public void reload() {
        if (this.configMissing) {
            return;
        }
        this.config = new JsonContainer(new ConfigFile(this.filePath));
        setupConfig();
        JacksonUtils.mergeDeclaredFieldsFromJson(this, config);
        if (this.configMissing) {
            return;
        }
        super.setConditionalData(data.getConditionalData(), "modal", ElementError.getAllWithout(ElementError.INVALID_ARGS));
    }

    private void setupConfig() {
        ForgedModalData emptyData = new ForgedModalData();
        this.data = config.getOrSetDefault("properties", ForgedModalData.class, emptyData);
        if (this.data == emptyData) {
            this.configMissing = true;
        }
    }

    @Override
    public abstract void onSubmission(@NotNull ModalContext context);

}

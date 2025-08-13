package me.mrepiko.cymric.elements.components.button;

import lombok.Getter;
import lombok.experimental.Delegate;
import me.mrepiko.cymric.config.ConfigFile;
import me.mrepiko.cymric.context.components.ButtonContext;
import me.mrepiko.cymric.elements.ElementError;
import me.mrepiko.cymric.elements.components.RowComponent;
import me.mrepiko.cymric.elements.components.ComponentHolder;
import me.mrepiko.cymric.elements.components.button.data.ForgedButtonData;
import me.mrepiko.cymric.jackson.JsonContainer;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.mics.Constants;
import me.mrepiko.cymric.jackson.JacksonUtils;
import me.mrepiko.cymric.mics.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class GenericButton extends ComponentHolder<ForgedButtonData> implements ButtonTemplate {

    private final String filePath;

    protected final String id;
    @Delegate
    protected JsonContainer config;
    protected ForgedButtonData data;

    private boolean configMissing;

    public GenericButton(@NotNull String id) {
        this.id = id;
        this.filePath = Constants.BUTTON_CONFIGURATION_FOLDER_PATH + id + ".json";
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
        super.setConditionalData(data.getConditionalData(), "button", ElementError.getAllWithout(ElementError.INVALID_ARGS));
        super.setTimeoutableData(data.getTimeoutableElementData());
        super.setComponentData(data.getComponentData());
    }

    private void setupConfig() {
        ForgedButtonData emptyData = new ForgedButtonData();
        this.data = config.getOrSetDefault("properties", ForgedButtonData.class, emptyData);
        if (this.data == emptyData) {
            this.configMissing = true;
        }
    }

    @NotNull
    @Override
    public RowComponent getRowComponent(@NotNull PlaceholderMap map, @Nullable Object overriddenData) {
        String uniqueId = Utils.generateUniqueComponentId(id);
        if (!(overriddenData instanceof ForgedButtonData overriddenButtonData)) {
            return new RowComponent(data.getButton(uniqueId, map), data.getRowIndex());
        }
        return new RowComponent(overriddenButtonData.getButton(uniqueId, map), overriddenButtonData.getRowIndex());
    }

    @Override
    public abstract void onInteraction(@NotNull ButtonContext context);

}

package me.mrepiko.cymric.elements.components.button;

import lombok.Getter;
import me.mrepiko.cymric.context.components.ButtonContext;
import me.mrepiko.cymric.elements.ElementError;
import me.mrepiko.cymric.elements.components.RowComponent;
import me.mrepiko.cymric.elements.components.ComponentLoader;
import me.mrepiko.cymric.elements.components.button.data.ForgedButtonData;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.mics.Constants;
import me.mrepiko.cymric.mics.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class GenericButton extends ComponentLoader<ForgedButtonData> implements ButtonHandler {

    public GenericButton(@NotNull String id) {
        super(id, Constants.BUTTON_CONFIGURATION_FOLDER_PATH);
    }

    @Override
    public void reload() {
        super.reload();
        if (this.configMissing) {
            return;
        }
        super.setConditionalData(data.getConditionalData(), "button", ElementError.getAllWithout(ElementError.INVALID_ARGS));
        super.setTimeoutableData(data.getTimeoutableElementData());
        super.setComponentData(data.getComponentData());
    }

    @Override
    public void initializeData() {
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

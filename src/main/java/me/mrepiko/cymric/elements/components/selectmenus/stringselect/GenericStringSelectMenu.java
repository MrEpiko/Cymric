package me.mrepiko.cymric.elements.components.selectmenus.stringselect;

import lombok.Getter;
import lombok.experimental.Delegate;
import me.mrepiko.cymric.config.ConfigFile;
import me.mrepiko.cymric.context.components.StringSelectMenuContext;
import me.mrepiko.cymric.elements.ElementError;
import me.mrepiko.cymric.elements.components.RowComponent;
import me.mrepiko.cymric.elements.components.ComponentHolder;
import me.mrepiko.cymric.elements.components.selectmenus.stringselect.data.ForgedStringSelectMenuData;
import me.mrepiko.cymric.jackson.JsonContainer;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.mics.Constants;
import me.mrepiko.cymric.jackson.JacksonUtils;
import me.mrepiko.cymric.mics.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class GenericStringSelectMenu extends ComponentHolder<ForgedStringSelectMenuData> implements StringSelectMenuTemplate {

    private final String filePath;

    protected final String id;
    @Delegate
    protected JsonContainer config;
    protected ForgedStringSelectMenuData data;

    private boolean configMissing;

    public GenericStringSelectMenu(@NotNull String id) {
        this.id = id;
        this.filePath = Constants.STRING_SELECT_MENU_CONFIGURATION_FOLDER_PATH + id + ".json";
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
        super.setConditionalData(data.getConditionalData(), "string_select_menu", ElementError.getAllWithout(ElementError.INVALID_ARGS));
        super.setTimeoutableData(data.getTimeoutableElementData());
        super.setComponentData(data.getComponentData());
    }

    private void setupConfig() {
        ForgedStringSelectMenuData emptyData = new ForgedStringSelectMenuData();
        this.data = config.getOrSetDefault("properties", ForgedStringSelectMenuData.class, emptyData);
        if (this.data == emptyData) {
            this.configMissing = true;
        }
    }

    @NotNull
    @Override
    public RowComponent getRowComponent(@NotNull PlaceholderMap map, @Nullable Object overriddenData) {
        String uniqueId = Utils.generateUniqueComponentId(id);
        if (!(overriddenData instanceof ForgedStringSelectMenuData overriddenMenuData)) {
            return new RowComponent(data.getStringSelectMenu(uniqueId, map), data.getRowIndex());
        }
        return new RowComponent(overriddenMenuData.getStringSelectMenu(uniqueId, map), overriddenMenuData.getRowIndex());
    }

    @Override
    public abstract void onInteraction(@NotNull StringSelectMenuContext context);

}

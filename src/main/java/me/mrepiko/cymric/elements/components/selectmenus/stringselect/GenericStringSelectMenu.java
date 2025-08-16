package me.mrepiko.cymric.elements.components.selectmenus.stringselect;

import lombok.Getter;
import me.mrepiko.cymric.context.components.StringSelectMenuContext;
import me.mrepiko.cymric.elements.ElementError;
import me.mrepiko.cymric.elements.components.RowComponent;
import me.mrepiko.cymric.elements.components.ComponentLoader;
import me.mrepiko.cymric.elements.components.selectmenus.stringselect.data.ForgedStringSelectMenuData;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.mics.Constants;
import me.mrepiko.cymric.mics.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class GenericStringSelectMenu extends ComponentLoader<ForgedStringSelectMenuData> implements StringSelectMenuTemplate {

    public GenericStringSelectMenu(@NotNull String id) {
        super(id, Constants.STRING_SELECT_MENU_CONFIGURATION_FOLDER_PATH);
    }

    @Override
    public void reload() {
        super.reload();
        if (this.configMissing) {
            return;
        }
        super.setConditionalData(data.getConditionalData(), "string_select_menu", ElementError.getAllWithout(ElementError.INVALID_ARGS));
        super.setTimeoutableData(data.getTimeoutableElementData());
        super.setComponentData(data.getComponentData());
    }

    @Override
    public void initializeData() {
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

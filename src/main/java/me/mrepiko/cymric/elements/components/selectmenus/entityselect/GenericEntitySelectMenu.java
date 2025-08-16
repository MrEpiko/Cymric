package me.mrepiko.cymric.elements.components.selectmenus.entityselect;

import lombok.Getter;
import me.mrepiko.cymric.context.components.EntitySelectMenuContext;
import me.mrepiko.cymric.elements.ElementError;
import me.mrepiko.cymric.elements.components.RowComponent;
import me.mrepiko.cymric.elements.components.ComponentLoader;
import me.mrepiko.cymric.elements.components.selectmenus.entityselect.data.ForgedEntitySelectMenuData;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.mics.Constants;
import me.mrepiko.cymric.mics.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class GenericEntitySelectMenu extends ComponentLoader<ForgedEntitySelectMenuData> implements EntitySelectMenuTemplate {

    public GenericEntitySelectMenu(@NotNull String id) {
        super(id, Constants.ENTITY_SELECT_MENU_CONFIGURATION_FOLDER_PATH);
    }

    @Override
    public void reload() {
        super.reload();
        if (this.configMissing) {
            return;
        }
        super.setConditionalData(data.getConditionalData(), "entity_select_menu", ElementError.getAllWithout(ElementError.INVALID_ARGS));
        super.setTimeoutableData(data.getTimeoutableElementData());
        super.setComponentData(data.getComponentData());
    }

    @Override
    public void initializeData() {
        ForgedEntitySelectMenuData emptyData = new ForgedEntitySelectMenuData();
        this.data = config.getOrSetDefault("properties", ForgedEntitySelectMenuData.class, emptyData);
        if (this.data == emptyData) {
            this.configMissing = true;
        }
    }

    @NotNull
    @Override
    public RowComponent getRowComponent(@NotNull PlaceholderMap map, @Nullable Object overriddenData) {
        String uniqueId = Utils.generateUniqueComponentId(id);
        if (!(overriddenData instanceof ForgedEntitySelectMenuData overriddenMenuData)) {
            return new RowComponent(data.getEntitySelectMenu(uniqueId, map), data.getRowIndex());
        }
        return new RowComponent(overriddenMenuData.getEntitySelectMenu(uniqueId, map), overriddenMenuData.getRowIndex());
    }

    @Override
    public abstract void onInteraction(@NotNull EntitySelectMenuContext context);

}

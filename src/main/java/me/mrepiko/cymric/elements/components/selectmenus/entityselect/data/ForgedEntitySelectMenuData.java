package me.mrepiko.cymric.elements.components.selectmenus.entityselect.data;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.*;
import lombok.experimental.Delegate;
import me.mrepiko.cymric.annotations.SupportsDefaultOverriding;
import me.mrepiko.cymric.elements.components.ForgedComponentDataContainer;
import me.mrepiko.cymric.elements.data.*;
import me.mrepiko.cymric.elements.components.selectmenus.SelectMenuData;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.mics.Utils;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SupportsDefaultOverriding
public class ForgedEntitySelectMenuData implements ForgedComponentDataContainer {

    @JsonUnwrapped
    @Delegate
    private EntitySelectMenuData entitySelectMenuData = new EntitySelectMenuData();

    @JsonUnwrapped
    @Delegate
    private SelectMenuData selectMenuData = new SelectMenuData();

    @JsonUnwrapped
    @Delegate
    private ConditionalData conditionalData = new ConditionalData();

    @JsonUnwrapped
    @Delegate
    private ElementData elementData = new ElementData();

    @JsonUnwrapped
    @Delegate
    private DeferrableElementData deferrableElementData = new DeferrableElementData();

    @JsonUnwrapped
    @Delegate
    private ComponentData componentData = new ComponentData();

    @JsonUnwrapped
    @Delegate
    private TimeoutableElementData timeoutableElementData = new TimeoutableElementData();

    @NotNull
    public EntitySelectMenu getEntitySelectMenu(@NotNull String id, @Nullable PlaceholderMap map) {
        EntitySelectMenu.SelectTarget selectTarget = entitySelectMenuData.getSelectTarget();

        EntitySelectMenu entitySelectMenu = EntitySelectMenu.create(id, selectTarget)
                .setPlaceholder(Utils.applyPlaceholders(map, selectMenuData.getPlaceholder()))
                .setMinValues(selectMenuData.getMinOptions())
                .setMaxValues(selectMenuData.getMaxOptions())
                .build();
        componentData.syncActionComponent(entitySelectMenu);
        return entitySelectMenu;
    }

}

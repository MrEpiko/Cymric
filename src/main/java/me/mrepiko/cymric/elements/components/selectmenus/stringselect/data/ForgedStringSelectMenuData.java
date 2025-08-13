package me.mrepiko.cymric.elements.components.selectmenus.stringselect.data;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import me.mrepiko.cymric.annotations.SupportsDefaultOverriding;
import me.mrepiko.cymric.elements.components.ForgedComponentDataContainer;
import me.mrepiko.cymric.elements.components.selectmenus.SelectMenuData;
import me.mrepiko.cymric.elements.data.*;
import me.mrepiko.cymric.mics.Utils;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SupportsDefaultOverriding
public class ForgedStringSelectMenuData implements ForgedComponentDataContainer {

    @JsonUnwrapped
    @Delegate
    private StringSelectMenuData stringSelectMenuData = new StringSelectMenuData();

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
    public List<SelectOption> getAssembledOptions(@Nullable PlaceholderMap map) {
        return stringSelectMenuData.getOptions().stream()
                .map(option -> option.getAssembled(map))
                .toList();
    }

    @NotNull
    public StringSelectMenu getStringSelectMenu(@NotNull String id, @Nullable PlaceholderMap map) {
        StringSelectMenu stringSelectMenu = StringSelectMenu.create(id)
                .setPlaceholder(Utils.applyPlaceholders(map, selectMenuData.getPlaceholder()))
                .setMinValues(selectMenuData.getMinOptions())
                .setMaxValues(selectMenuData.getMaxOptions())
                .addOptions(getAssembledOptions(map))
                .build();
        componentData.syncActionComponent(stringSelectMenu);
        return stringSelectMenu;
    }
}

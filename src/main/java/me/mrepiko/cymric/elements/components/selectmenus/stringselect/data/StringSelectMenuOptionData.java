package me.mrepiko.cymric.elements.components.selectmenus.stringselect.data;

import lombok.*;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.mics.Utils;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StringSelectMenuOptionData {

    private String label;
    private String value;
    private String description;
    private String emojiFormatted;
    private boolean defaultValue;

    public SelectOption getAssembled(@Nullable PlaceholderMap map) {
        SelectOption selectOption = SelectOption.of(
                Utils.applyPlaceholders(map, label),
                Utils.applyPlaceholders(map, value)
        );
        if (description != null && !description.isEmpty()) {
            selectOption = selectOption.withDescription(Utils.applyPlaceholders(map, description));
        }
        if (emojiFormatted != null && !emojiFormatted.isEmpty()) {
            selectOption = selectOption.withEmoji(Emoji.fromFormatted(Utils.applyPlaceholders(map, emojiFormatted)));
        }
        if (defaultValue) {
            selectOption = selectOption.withDefault(true);
        }
        return selectOption;
    }

}

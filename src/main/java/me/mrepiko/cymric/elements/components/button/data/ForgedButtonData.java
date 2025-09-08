package me.mrepiko.cymric.elements.components.button.data;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Delegate;
import me.mrepiko.cymric.annotations.SupportsDefaultOverriding;
import me.mrepiko.cymric.elements.components.ForgedComponentDataContainer;
import me.mrepiko.cymric.elements.data.ConditionalData;
import me.mrepiko.cymric.elements.data.DeferrableElementData;
import me.mrepiko.cymric.elements.data.ElementData;
import me.mrepiko.cymric.elements.data.TimeoutableElementData;
import me.mrepiko.cymric.elements.data.ComponentData;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.mics.Utils;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SupportsDefaultOverriding
public class ForgedButtonData implements ForgedComponentDataContainer {

    @JsonUnwrapped
    @Delegate
    private ButtonData buttonData = new ButtonData();

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
    public Button getButton(@NotNull String id, @Nullable PlaceholderMap map) {
        String label = Utils.applyPlaceholders(map, buttonData.getLabel());
        String url = Utils.applyPlaceholders(map, buttonData.getUrl());
        ButtonStyle style = buttonData.getStyle();
        String emojiFormatted = Utils.applyPlaceholders(map, buttonData.getEmojiFormatted());

        Button button = Button.of(style, (style == ButtonStyle.LINK && url != null && !url.isEmpty()) ? url : id, Utils.applyPlaceholders(map, label));
        if (emojiFormatted != null && !emojiFormatted.isEmpty()) {
            button = button.withEmoji(Emoji.fromFormatted(emojiFormatted));
        }
        componentData.syncActionComponent(button);
        return button;
    }

}

package me.mrepiko.cymric.elements.modal.data;

import lombok.*;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.mics.Utils;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModalTextFieldData {

    private String id;
    private String placeholder;
    private String label;
    private int minLength = 0;
    private int maxLength;
    private boolean required;
    private String value;
    private TextInputStyle style;

    public TextInput getAssembled(PlaceholderMap map) {
        TextInput.Builder builder = TextInput.create(id, Utils.applyPlaceholders(map, label), style)
                .setPlaceholder(Utils.applyPlaceholders(map, placeholder))
                .setMinLength(minLength)
                .setMaxLength(maxLength);
        builder.setRequired(required);
        if (value != null && !value.isEmpty()) {
            builder.setValue(Utils.applyPlaceholders(map, value));
        }
        return builder.build();
    }

}

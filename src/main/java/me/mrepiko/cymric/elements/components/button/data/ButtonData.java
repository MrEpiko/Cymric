package me.mrepiko.cymric.elements.components.button.data;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ButtonData {

    private String label;
    private String url;
    private ButtonStyle style;
    private String emojiFormatted;

}

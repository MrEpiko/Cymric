package me.mrepiko.cymric.response.data.embed;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.mrepiko.cymric.mics.Utils;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FieldData {

    private String name;
    private String value;
    private boolean inline;
    private boolean blank;

    @NotNull
    public MessageEmbed.Field getField(@Nullable PlaceholderMap map, @NotNull String truncationIndicator) {
        if (blank) {
            return new MessageEmbed.Field("\u200e", "\u200e", inline);
        }
        return new MessageEmbed.Field(
                Utils.truncateString(Utils.applyPlaceholders(map, name), 256, truncationIndicator),
                Utils.truncateString(Utils.applyPlaceholders(map, value), 1024, truncationIndicator),
                inline
        );
    }

}

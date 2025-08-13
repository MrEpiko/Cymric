package me.mrepiko.cymric.response.data.embed;

import lombok.*;
import me.mrepiko.cymric.mics.Utils;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageEmbedData {

    private String title;
    private String titleUrl;
    private String description;
    private String footerText;
    private String footerIconUrl;
    private String authorText;
    private String authorIconUrl;
    private String authorUrl;
    private String thumbnailUrl;
    private String imageUrl;
    private String color; // Hex color code, e.g., "#FF5733"
    private String timestampSeconds;
    private List<FieldData> fields;

    @Nullable
    public Color getColorAsColor(@Nullable PlaceholderMap map) {
        if (color == null || color.isEmpty()) {
            return null;
        }
        try {
            return Color.decode(Utils.applyPlaceholders(map, color));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @NotNull
    public EmbedBuilder getEmbedBuilder(@Nullable PlaceholderMap map, @NotNull String truncationIndicator) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (title != null && !title.isEmpty()) {
            URL titleUrlObj = null;
            if (titleUrl != null && !titleUrl.isEmpty()) {
                try {
                    titleUrlObj = new URI(Utils.applyPlaceholders(map, titleUrl)).toURL();
                } catch (Exception ignored) {}
            }
            embedBuilder.setTitle(
                    Utils.truncateString(Utils.applyPlaceholders(map, title), 256, truncationIndicator),
                    titleUrlObj != null ? titleUrlObj.toString() : null)
            ;
        }
        if (description != null && !description.isEmpty()) {
            embedBuilder.setDescription(
                    Utils.truncateString(Utils.applyPlaceholders(map, description), 4096, truncationIndicator)
            );
        }
        if (footerText != null && !footerText.isEmpty()) {
            URL footerIconUrlObj = null;
            if (footerIconUrl != null && !footerIconUrl.isEmpty()) {
                try {
                    footerIconUrlObj = new URI(Utils.applyPlaceholders(map, footerIconUrl)).toURL();
                } catch (Exception ignored) {}
            }
            embedBuilder.setFooter(
                    Utils.truncateString(Utils.applyPlaceholders(map, footerText), 2048, truncationIndicator),
                    footerIconUrlObj != null ? footerIconUrlObj.toString() : null
            );
        }
        if (authorText != null && !authorText.isEmpty()) {
            URL authorIconUrlObj = null;
            if (authorIconUrl != null && !authorIconUrl.isEmpty()) {
                try {
                    authorIconUrlObj = new URI(Utils.applyPlaceholders(map, authorIconUrl)).toURL();
                } catch (Exception ignored) {}
            }
            URL authorUrlObj = null;
            if (authorUrl != null && !authorUrl.isEmpty()) {
                try {
                    authorUrlObj = new URI(Utils.applyPlaceholders(map, authorUrl)).toURL();
                } catch (Exception ignored) {}
            }
            embedBuilder.setAuthor(
                    Utils.truncateString(Utils.applyPlaceholders(map, authorText), 256, truncationIndicator),
                    authorUrlObj != null ? authorUrlObj.toString() : null,
                    authorIconUrlObj != null ? authorIconUrlObj.toString() : null
            );
        }
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            try {
                embedBuilder.setThumbnail(new URI(Utils.applyPlaceholders(map, thumbnailUrl)).toURL().toString());
            } catch (Exception ignored) {}
        }
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                embedBuilder.setImage(new URI(Utils.applyPlaceholders(map, imageUrl)).toURL().toString());
            } catch (Exception ignored) {}
        }
        if (color != null && !color.isEmpty()) {
            Color embedColor = getColorAsColor(map);
            if (embedColor != null) {
                embedBuilder.setColor(embedColor);
            }
        }
        if (timestampSeconds != null && !timestampSeconds.isEmpty()) {
            embedBuilder.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.decode(Utils.applyPlaceholders(map, timestampSeconds)) * 1000), ZoneId.systemDefault()));
        }
        if (fields != null && !fields.isEmpty()) {
            for (FieldData field : fields) {
                if (field.isBlank()) {
                    embedBuilder.addBlankField(field.isInline());
                    continue;
                }
                String fieldName = field.getName();
                String value = field.getValue();

                if ((fieldName == null || fieldName.isEmpty()) && (value == null || value.isEmpty())) {
                    continue;
                }

                embedBuilder.addField(
                        Utils.truncateString(Utils.applyPlaceholders(map, fieldName), 256, truncationIndicator),
                        Utils.truncateString(Utils.applyPlaceholders(map, value), 1024, truncationIndicator),
                        field.isInline()
                );
            }
        }
        return embedBuilder;
    }

}

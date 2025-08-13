package me.mrepiko.cymric.placeholders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AllArgsConstructor
@Getter
public enum ListStyle {
    SPACE(" ", false),
    COMMA(", ", false),
    NEWLINE_DASH("- ", true),
    NEWLINE_NUMBERED("1. ", true),;

    private final String delimiter;
    private final boolean newline;

    @NotNull
    public String getFormatted(@NotNull List<?> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (newline) {
                sb.append("\n");
            }
            if (this == NEWLINE_NUMBERED) {
                sb.append(i + 1).append(". ");
            }
            if (!newline && i == list.size() - 1) {
                sb.append("and ");
            }
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString().trim();
    }

}

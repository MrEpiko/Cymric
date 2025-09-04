package me.mrepiko.cymric.placeholders;

import me.mrepiko.cymric.context.plain.MessageChannelContext;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public interface PlaceholderMap {
    @Nullable
    MessageChannelContext getContext();

    @NotNull
    String applyPlaceholders(@NotNull String input);

    @NotNull
    String applyPlaceholders(@NotNull StringBuilder stringBuilder);

    @NotNull
    Map<String, String> getPlaceholders();

    void put(@NotNull String identifier, @Nullable String value);

    void put(@NotNull String identifier, @Nullable Number number);

    void put(@NotNull String identifier, boolean bool);

    void put(@NotNull String identifier, @NotNull Color color);

    void put(@NotNull String identifier, @NotNull MessageChannelContext context);

    void put(@NotNull String identifier, @NotNull User user);

    void put(@NotNull String identifier, @NotNull Member member);

    void put(@NotNull String identifier, @NotNull MessageChannel messageChannel);

    void put(@NotNull String identifier, @NotNull Guild guild);

    void put(@NotNull String identifier, @NotNull Role role);

    void put(@NotNull String identifier, @NotNull Message message);

    void put(@NotNull String identifier, @NotNull MessageEmbed embed);

    void put(@NotNull String identifier, @NotNull OffsetDateTime offsetDateTime);

    void put(@NotNull String identifier, @NotNull Command command);

    void put(@NotNull String identifier, @Nullable List<?> list, @NotNull ListStyle style, @NotNull String defaultValue);

    void put(@NotNull String identifier, @NotNull Timestamp timestamp);

    default void put(@NotNull String identifier, @NotNull Placeholderable placeholderable) {
        placeholderable.appendToPlaceholderMap(identifier, this);
    }
}

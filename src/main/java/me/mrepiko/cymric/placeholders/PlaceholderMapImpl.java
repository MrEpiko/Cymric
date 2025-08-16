package me.mrepiko.cymric.placeholders;

import lombok.Getter;
import me.mrepiko.cymric.CymricApi;
import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.config.main.CymricConfig;
import me.mrepiko.cymric.context.plain.MessageChannelContext;
import me.mrepiko.cymric.context.plain.MessageContext;
import me.mrepiko.cymric.elements.command.CommandLoader;
import me.mrepiko.cymric.mics.Utils;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.detached.IDetachableEntity;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceholderMapImpl implements PlaceholderMap {

    private final CymricApi instance = DiscordBot.getInstance();
    private final CymricConfig config = instance.getConfig();

    @Getter
    private final Map<String, String> placeholders = new HashMap<>();

    @Nullable
    private final MessageChannelContext context;

    protected PlaceholderMapImpl(
            @Nullable MessageChannelContext context,
            boolean includeContextPlaceholders,
            boolean includeConstantPlaceholders,
            boolean includeCommandPlaceholders,
            @Nullable PlaceholderMap map
    ) {
        this.context = context;
        putDefaultPlaceholders();
        if (includeContextPlaceholders && context != null) {
            put("ctx", context);
        }
        if (includeConstantPlaceholders) {
            config.getConstants().forEach((key, value) -> put("c_" + key, value));
        }
        if (includeCommandPlaceholders) {
            for (CommandLoader<?> holder : instance.getCommandManager().getRegistered()) {
                Command discordCommand = holder.getDiscordCommand();
                if (discordCommand == null) {
                    continue;
                }
                put("cmd_", discordCommand);
            }
        }
        if (map != null) {
            map.getPlaceholders().forEach((key, value) -> {
                if (!placeholders.containsKey(key)) {
                    placeholders.put(key, value);
                }
            });
        }
    }

    private void putDefaultPlaceholders() {
        SelfUser selfUser = instance.getFirstShard().getSelfUser();
        put("current_timestamp", Utils.getCurrentTimeSeconds());
        put("current_timestamp_millis", System.currentTimeMillis());
        put("bot_name", selfUser.getId());
        put("bot_id", selfUser.getId());
    }

    @Nullable
    @Override
    public MessageChannelContext getContext() {
        return context;
    }

    @NotNull
    @Override
    public String applyPlaceholders(@NotNull String input) {
        for (String key : placeholders.keySet()) {
            String value = placeholders.get(key);
            if (value == null) {
                value = "null";
            }
            input = input.replace("{" + key + "}", value);
        }
        return input;
    }

    @NotNull
    @Override
    public String applyPlaceholders(@NotNull StringBuilder stringBuilder) {
        return applyPlaceholders(stringBuilder.toString());
    }

    @Override
    public void put(@NotNull String identifier, @Nullable String value) {
        placeholders.put(identifier, value);
    }

    @Override
    public void put(@NotNull String identifier, @Nullable Number number) {
        put(identifier, String.valueOf(number));
        switch (number) {
            case Integer i -> put(identifier + "_f", String.format("%,d", i));
            case Long l -> put(identifier + "_f", String.format("%,d", l));
            case Double d -> put(identifier + "_f", String.format("%,.2f", d));
            case Float f -> put(identifier + "_f", String.format("%,.2f", f));
            case Byte b -> put(identifier + "_f", String.format("%,d", b));
            case Short s -> put(identifier + "_f", String.format("%,d", s));
            case null, default -> put(identifier + "_f", "null");
        }
    }

    @Override
    public void put(@NotNull String identifier, boolean bool) {
        put(identifier, String.valueOf(bool));
    }

    @Override
    public void put(@NotNull String identifier, @NotNull Color color) {
        put(identifier, String.format("#%06x", color.getRGB() & 0x00FFFFFF));
    }

    @Override
    public void put(@NotNull String identifier, @NotNull MessageChannelContext context) {
        User user = context.getUser();
        Guild guild = context.getGuild();

        if (context instanceof MessageContext messageContext) {
            Message message = messageContext.getMessage();
            put(identifier + "_message", message);
        }

        put(identifier + "_channel", context.getMessageChannel());
        if (user != null) {
            put(identifier + "_user", user);
        }
        if (guild != null) {
            put(identifier + "_guild", guild);
        }
    }

    @Override
    public void put(@NotNull String identifier, @NotNull User user) {
        put(identifier + "_id", user.getId());
        put(identifier + "_mention", user.getAsMention());
        put(identifier + "_name", user.getName());
        put(identifier + "_effective_name", user.getEffectiveName());
        put(identifier + "_global_name", user.getGlobalName());
        put(identifier + "_avatar_url", user.getAvatarUrl());
        put(identifier + "_effective_avatar_url", user.getEffectiveAvatarUrl());
        put(identifier + "_discriminator", user.getDiscriminator());
    }

    @Override
    public void put(@NotNull String identifier, @NotNull Member member) {
        if (isDetached(member)) {
            return;
        }
        put(identifier, member.getUser());
    }

    @Override
    public void put(@NotNull String identifier, @NotNull MessageChannel messageChannel) {
        put(identifier + "_id", messageChannel.getId());

        if (isDetached(messageChannel)) {
            return;
        }
        put(identifier + "_mention", messageChannel.getAsMention());
        put(identifier + "_name", messageChannel.getName());
    }

    @Override
    public void put(@NotNull String identifier, @NotNull Guild guild) {
        put(identifier + "_id", guild.getId());

        if (isDetached(guild)) {
            return;
        }
        put(identifier + "_name", guild.getName());
        put(identifier + "_icon_url", guild.getIconUrl());
        put(identifier + "_splash_url", guild.getSplashUrl());
        put(identifier + "_banner_url", guild.getBannerUrl());
    }

    @Override
    public void put(@NotNull String identifier, @NotNull Role role) {
        put(identifier + "_id", role.getId());

        if (isDetached(role)) {
            return;
        }
        put(identifier + "_mention", role.getAsMention());
        put(identifier + "_name", role.getName());
        put(identifier + "_color", String.valueOf(role.getColorRaw()));
        put(identifier + "_color_hex", String.format("#%06X", (0xFFFFFF & role.getColorRaw())));
    }

    @Override
    public void put(@NotNull String identifier, @NotNull Message message) {
        put(identifier + "_id", message.getId());
        put(identifier + "_content", message.getContentRaw());
        put(identifier + "_timestamp", message.getTimeCreated().toString());
        put(identifier + "_author", message.getAuthor());
    }

    @Override
    public void put(@NotNull String identifier, @NotNull MessageEmbed embed) {
        OffsetDateTime timestamp = embed.getTimestamp();

        if (timestamp != null) {
            put(identifier + "_timestamp", timestamp);
        }
        put(identifier + "_title", embed.getTitle());
        put(identifier + "_description", embed.getDescription());
        put(identifier + "_url", embed.getUrl());
        put(identifier + "_color", String.valueOf(embed.getColorRaw()));
        put(identifier + "_color_hex", String.format("#%06X", (0xFFFFFF & embed.getColorRaw())));
        put(identifier + "_footer_text", embed.getFooter() != null ? embed.getFooter().getText() : null);
        put(identifier + "_footer_icon_url", embed.getFooter() != null ? embed.getFooter().getIconUrl() : null);
        put(identifier + "_author_name", embed.getAuthor() != null ? embed.getAuthor().getName() : null);
        put(identifier + "_author_url", embed.getAuthor() != null ? embed.getAuthor().getUrl() : null);
        put(identifier + "_author_icon_url", embed.getAuthor() != null ? embed.getAuthor().getIconUrl() : null);
        put(identifier + "_thumbnail_url", embed.getThumbnail() != null ? embed.getThumbnail().getUrl() : null);
        put(identifier + "_image_url", embed.getImage() != null ? embed.getImage().getUrl() : null);
    }

    @Override
    public void put(@NotNull String identifier, @NotNull OffsetDateTime offsetDateTime) {
        put(identifier + "_timestamp", String.valueOf(offsetDateTime.toEpochSecond()));
        put(identifier + "_timestamp_millis", String.valueOf(offsetDateTime.toInstant().toEpochMilli()));
        put(identifier + "_date", offsetDateTime.format(DateTimeFormatter.ofPattern(config.getDefaultDateFormat())));
        put(identifier + "_time", offsetDateTime.format(DateTimeFormatter.ofPattern(config.getDefaultTimeFormat())));
    }

    @Override
    public void put(@NotNull String identifier, @NotNull Command command) {
        String discordId = command.getId();
        put(identifier + "_discord_id", discordId);
        put(identifier + "_id", command.getId());
        put(identifier + "_mention", "<#" + discordId + ">");
    }

    @Override
    public void put(@NotNull String identifier, @Nullable List<?> list, @NotNull ListStyle style, @NotNull String defaultValue) {
        if (list == null || list.isEmpty()) {
            put(identifier, defaultValue);
            return;
        }
        put(identifier, style.getFormatted(list));
    }

    private boolean isDetached(@NotNull IDetachableEntity entity) {
        return entity.isDetached();
    }

}

package me.mrepiko.cymric.elements;

import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.config.ConfigFile;
import me.mrepiko.cymric.context.plain.MessageChannelContext;
import me.mrepiko.cymric.elements.containers.ConditionalDataContainer;
import me.mrepiko.cymric.elements.data.ConditionalData;
import me.mrepiko.cymric.elements.plain.Conditionable;
import me.mrepiko.cymric.jackson.JsonContainer;
import me.mrepiko.cymric.mics.Constants;
import me.mrepiko.cymric.mics.Utils;
import me.mrepiko.cymric.placeholders.ListStyle;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.response.ResponseBuilder;
import me.mrepiko.cymric.response.data.ResponseData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.mrepiko.cymric.mics.Utils.applyPlaceholders;

public abstract class ConditionalElementLoader<T extends ConditionalDataContainer> extends ElementLoader<T> implements Conditionable {

    private ConditionalData parentConditionalData;
    private final Map<String, Long> cooldowns = new HashMap<>();

    public ConditionalElementLoader(@NotNull String id, @NotNull String folderPath) {
        super(id, folderPath);
    }

    @NotNull
    protected ResponseData getErrorResponseData(@NotNull ElementError elementError) {
        return parentConditionalData.getErrorResponses().getOrDefault(elementError, new ResponseData());
    }

    protected void setConditionalData(@NotNull ConditionalData conditionalData, @NotNull String elementId, @NotNull List<ElementError> possibleErrors) {
        this.parentConditionalData = conditionalData;

        JsonContainer errorHandlers = new JsonContainer(new ConfigFile(
                Constants.ERROR_RESPONSES_CONFIGURATION_FOLDER_PATH + elementId + ".json",
                true)
        );
        Map<ElementError, ResponseData> errorResponses = conditionalData.getErrorResponses();

        for (ElementError error : possibleErrors) {
            ResponseData responseData = errorHandlers.getOrSetDefault(error.name(), ResponseData.class, new ResponseData());
            if (!errorResponses.containsKey(error)) {
                errorResponses.put(error, responseData);
            }
        }
    }

    @Override
    public boolean check(@NotNull MessageChannelContext context) {
        return check(context, this.parentConditionalData);
    }

    @Override
    public boolean check(@NotNull MessageChannelContext context, @NotNull ConditionalData overriddenData) {
        PlaceholderMap map = context.getPlaceholderMap();
        return checkIsEnabled(map, overriddenData)
                && checkUserCooldown(map, context)
                && checkAdmins(map, context, overriddenData)
                && checkIsTalkRequired(map, context, overriddenData)
                && checkRequiredUsers(map, context, overriddenData)
                && checkRequiredRoles(map, context, overriddenData)
                && checkRequiredChannels(map, context, overriddenData)
                && checkRequiredGuilds(map, context, overriddenData)
                && checkAllPermissions(map, context, overriddenData);
    }

    @Override
    public void setUserCooldown(@NotNull User user) {
        setUserCooldown(user, this.parentConditionalData);
    }

    @Override
    public void setUserCooldown(@NotNull User user, @NotNull ConditionalData overriddenData) {
        long currentTime = System.currentTimeMillis();
        cooldowns.put(user.getId(), currentTime + (long) overriddenData.getCooldownMillis());
    }

    private boolean checkIsEnabled(PlaceholderMap map, ConditionalData overriddenData) {
        if (overriddenData.isEnabled()) {
            return true;
        }

        ResponseData responseData = getErrorResponseData(ElementError.DISABLED);
        ResponseBuilder.create(map, responseData).buildAndSend();
        return false;
    }

    private boolean checkRequiredGuilds(PlaceholderMap map, MessageChannelContext context, ConditionalData overriddenData) {
        List<String> requiredGuildIds = overriddenData.getRequiredGuildIds();
        ResponseData responseData = getErrorResponseData(ElementError.NOT_IN_REQUIRED_GUILD);
        map.put("required_guild_ids", requiredGuildIds, ListStyle.NEWLINE_DASH, "- `N/A`");

        if (requiredGuildIds == null || requiredGuildIds.isEmpty()) {
            return true;
        }

        if (!context.isFromGuild()) {
            ResponseBuilder.create(map, responseData).buildAndSend();
            return false;
        }

        Guild guild = context.getGuild();
        applyPlaceholders(map, requiredGuildIds);
        boolean contains = contains(requiredGuildIds, (guild == null) ? null : guild.getId());
        if (!contains) {
            ResponseBuilder.create(map, responseData).buildAndSend();
            return false;
        }

        return true;
    }

    private boolean checkRequiredRoles(PlaceholderMap map, MessageChannelContext context, ConditionalData overriddenData) {
        List<String> requiredRoleIds = overriddenData.getRequiredRoleIds();
        ResponseData responseData = getErrorResponseData(ElementError.NO_REQUIRED_ROLES);
        map.put(
                "required_role_mentions",
                (requiredRoleIds == null) ? null : requiredRoleIds
                        .stream()
                        .map(r -> "<@&" + r + ">")
                        .toList(),
                ListStyle.NEWLINE_DASH,
                "- `N/A`"
        );

        if (requiredRoleIds == null || requiredRoleIds.isEmpty()) {
            return true;
        }

        applyPlaceholders(map, requiredRoleIds);
        if (!context.isFromGuild()) {
            ResponseBuilder.create(map, responseData).buildAndSend();
            return false;
        }

        Member member = context.getMember();
        if (member == null) {
            ResponseBuilder.create(map, responseData).buildAndSend();
            return false;
        }

        List<String> memberRoleIds = new ArrayList<>(member.getRoles()
                .stream()
                .map(ISnowflake::getId)
                .toList()
        );

        boolean containsAtLeastOne = containsAtLeastOne(memberRoleIds, requiredRoleIds);
        if (!containsAtLeastOne) {
            ResponseBuilder.create(map, responseData).buildAndSend();
            return false;
        }

        return true;
    }

    private boolean checkRequiredChannels(PlaceholderMap map, MessageChannelContext context, ConditionalData overriddenData) {
        List<String> requiredChannelIds = overriddenData.getRequiredChannelIds();
        ResponseData responseData = getErrorResponseData(ElementError.NOT_IN_REQUIRED_CHANNEL);
        map.put(
                "required_channel_mentions",
                (requiredChannelIds == null) ? null : requiredChannelIds
                        .stream()
                        .map(r -> "<#" + r + ">")
                        .toList(),
                ListStyle.NEWLINE_DASH,
                "- `N/A`"
        );

        if (requiredChannelIds == null || requiredChannelIds.isEmpty()) {
            return true;
        }

        applyPlaceholders(map, requiredChannelIds);
        boolean contains = contains(requiredChannelIds, context.getMessageChannel().getId());
        if (!contains) {
            ResponseBuilder.create(map, responseData).buildAndSend();
            return false;
        }

        return true;
    }

    private boolean checkRequiredUsers(PlaceholderMap map, MessageChannelContext context, ConditionalData overriddenData) {
        List<String> requiredUserIds = overriddenData.getRequiredUserIds();
        ResponseData responseData = getErrorResponseData(ElementError.NOT_REQUIRED_USER);
        map.put(
                "required_user_mentions",
                (requiredUserIds == null) ? null : requiredUserIds
                        .stream()
                        .map(r -> "<@" + r + ">")
                        .toList(),
                ListStyle.NEWLINE_DASH,
                "- `N/A`"
        );

        if (requiredUserIds == null || requiredUserIds.isEmpty()) {
            return true;
        }

        User user = context.getUser();
        applyPlaceholders(map, requiredUserIds);
        boolean contains = contains(requiredUserIds, user == null ? null : user.getId());
        if (!contains) {
            ResponseBuilder.create(map, responseData).buildAndSend();
            return false;
        }

        return true;
    }

    private boolean checkAdmins(PlaceholderMap map, MessageChannelContext context, ConditionalData overriddenData) {
        if (!overriddenData.isBotAdminRequired()) {
            return true;
        }

        ResponseData responseData = getErrorResponseData(ElementError.USER_NOT_ADMIN);
        List<String> adminIds = DiscordBot.getInstance().getConfig().getBotAdminIds();

        if (adminIds == null) {
            ResponseBuilder.create(map, responseData).buildAndSend();
            return false;
        }

        User user = context.getUser();
        boolean contains = contains(adminIds, user == null ? null : user.getId());
        if (!contains) {
            ResponseBuilder.create(map, responseData).buildAndSend();
            return false;
        }

        return true;
    }

    private boolean checkIsTalkRequired(PlaceholderMap map, MessageChannelContext context, ConditionalData overriddenData) {
        if (!overriddenData.isTalkRequired()) {
            return true;
        }

        ResponseData responseData = getErrorResponseData(ElementError.TALK_REQUIRED);
        MessageChannel channel = context.getMessageChannel();
        if (!channel.canTalk()) {
            ResponseBuilder.create(map, responseData).buildAndSend();
            return false;
        }

        return true;
    }

    private boolean checkUserCooldown(PlaceholderMap map, MessageChannelContext context) {
        User user = context.getUser();
        String id = user == null ? null : user.getId();
        long currentTimestamp = System.currentTimeMillis();

        if (id == null) {
            return false;
        }
        if (!cooldowns.containsKey(id)) {
            return true;
        }

        Long cooldownEndTimestamp = cooldowns.get(id);
        if (currentTimestamp >= cooldownEndTimestamp) {
            cooldowns.remove(id);
            return true;
        }

        double secondsLeft = (cooldownEndTimestamp - currentTimestamp) / 1000.0;
        map.put("cooldown_remaining", Utils.formatToTimeString((int) secondsLeft, false));
        map.put("cooldown_remaining_seconds", Math.floor(secondsLeft * 100) / 100); // Round to 2 decimal places.
        map.put("cooldown_expiry_timestamp", cooldownEndTimestamp / 1000);

        ResponseData responseData = getErrorResponseData(ElementError.ON_COOLDOWN);
        ResponseBuilder.create(map, responseData).buildAndSend();
        return false;
    }

    private boolean checkAllPermissions(PlaceholderMap map, MessageChannelContext context, ConditionalData overriddenData) {
        return checkPermissions(map, context, ElementError.USER_LACKS_PERMISSIONS, overriddenData.getRequiredInvokerPermissions())
                && checkPermissions(map, context, ElementError.BOT_LACKS_PERMISSIONS, overriddenData.getRequiredBotPermissions());
    }

    private boolean checkPermissions(PlaceholderMap map, MessageChannelContext context, ElementError error, @Nullable List<Permission> requiredPermissions) {
        map.put(
                "required_permissions",
                (requiredPermissions == null) ? null : requiredPermissions
                        .stream()
                        .map(Permission::getName)
                        .toList(),
                ListStyle.NEWLINE_DASH,
                "- `N/A`"
        );

        if (requiredPermissions == null) {
            return true;
        }

        ResponseData responseData = getErrorResponseData(error);
        if (!context.isFromGuild()) {
            ResponseBuilder.create(map, responseData).buildAndSend();
            return false;
        }

        GuildMessageChannel channel = context.getGuildMessageChannel();
        Member member = (error == ElementError.USER_LACKS_PERMISSIONS) ? context.getMember() : channel.getGuild().getSelfMember();
        if (member == null || !member.hasPermission(channel, requiredPermissions)) {
            ResponseBuilder.create(map, responseData).buildAndSend();
            return false;
        }

        return true;
    }

    private <V> boolean contains(@NotNull List<V> list, @Nullable V item) {
        return item != null && list.contains(item);
    }

    private <V> boolean containsAtLeastOne(@NotNull List<V> list, @NotNull Collection<V> items) {
        for (V item : items) {
            if (list.contains(item)) {
                return true;
            }
        }
        return false;
    }

}



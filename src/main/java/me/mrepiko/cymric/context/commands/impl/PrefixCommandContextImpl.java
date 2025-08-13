package me.mrepiko.cymric.context.commands.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.mrepiko.cymric.context.commands.PrefixCommandContext;
import me.mrepiko.cymric.discord.DiscordCache;
import me.mrepiko.cymric.elements.command.CommandHolder;
import me.mrepiko.cymric.elements.command.chat.GenericChatCommand;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class PrefixCommandContextImpl implements PrefixCommandContext {

    private final GenericChatCommand command;
    private final MessageReceivedEvent event;
    private final List<String> args;

    private int argIndex;
    private final HashMap<String, String> cachedOptions = new HashMap<>();

    private void incrementArgIndex() {
        if (argIndex < args.size()) {
            argIndex++;
        }
    }

    private boolean isCommandOptionRequired(String name) {
        OptionData option = getOptionData(name);
        if (option == null) {
            return false;
        }
        return option.isRequired();
    }

    @Nullable
    private OptionData getOptionData(String name) {
        List<OptionData> assembledOptions = command.getData().getAssembledOptions(null);
        if (assembledOptions == null || assembledOptions.isEmpty()) {
            return null;
        }
        for (OptionData option : assembledOptions) {
            if (option.getName().equalsIgnoreCase(name)) {
                return option;
            }
        }
        return null;
    }

    @Override
    public void resetArgIndex() {
        this.argIndex = 0;
    }

    @Nullable
    @Override
    public String getOptionAsString(@NotNull String option) {
        return getOptionAsString(option, null);
    }

    @Nullable
    @Override
    public String getOptionAsString(@NotNull String option, int pullArgs, @Nullable String defaultValue) {
        if (pullArgs == 0) {
            pullArgs = 5000; // Default to pulling all remaining arguments if none specified.
        }

        if (cachedOptions.containsKey(option)) {
            String value = cachedOptions.get(option);
            if (value != null) {
                return value;
            }
        }
        if (argIndex >= args.size()) {
            return defaultValue;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pullArgs; i++) {
            if (argIndex >= args.size()) {
                break;
            }
            sb.append(args.get(argIndex));
            if (i < pullArgs - 1) {
                sb.append(" ");
            }
            incrementArgIndex();
        }

        String argValue = sb.toString().trim();
        OptionData optionData = getOptionData(option);

        if (optionData == null) {
            throw new IllegalArgumentException("Option '" + option + "' not found in the command.");
        }
        return resolveOptionString(option, argValue, optionData, defaultValue);
    }

    @Override
    public String getOptionAsString(@NotNull String option, @Nullable String defaultValue) {
        if (cachedOptions.containsKey(option)) {
            String value = cachedOptions.get(option);
            if (value != null) {
                return value;
            }
        }
        if (argIndex >= args.size()) {
            return defaultValue;
        }

        OptionData optionData = getOptionData(option);
        if (optionData == null) {
            throw new IllegalArgumentException("Option '" + option + "' not found in the command.");
        }

        String argValue = args.get(argIndex).trim();
        incrementArgIndex();
        return resolveOptionString(option, argValue, optionData, defaultValue);
    }

    private String resolveOptionString(String option, String argValue, OptionData optionData, @Nullable String defaultValue) {
        if (optionData.getChoices().isEmpty()) {
            cachedOptions.put(option, argValue);
            return argValue;
        }
        for (Command.Choice choice : optionData.getChoices()) {
            if (choice.getName().equalsIgnoreCase(argValue)) {
                cachedOptions.put(option, choice.getAsString());
                return choice.getAsString();
            }
        }
        if (optionData.isRequired()) {
            return null;
        }
        return defaultValue;
    }

    @Nullable
    @Override
    public String getOptionAsString(@NotNull String option, int pullArgs) {
        return getOptionAsString(option, pullArgs, null);
    }

    @Nullable
    @Override
    public Integer getOptionAsInteger(@NotNull String option) {
        return getOptionAsInteger(option, null);
    }

    @Nullable
    @Override
    public Integer getOptionAsInteger(@NotNull String option, @Nullable Integer defaultValue) {
        String optionAsString = getOptionAsString(option, String.valueOf(defaultValue));
        if (optionAsString == null) {
            return (Integer) handleChecks(option, defaultValue);
        }
        try {
            return Integer.parseInt(optionAsString);
        } catch (NumberFormatException e) {
            return (Integer) handleChecks(option, defaultValue);
        }
    }

    @Nullable
    @Override
    public Double getOptionAsDouble(@NotNull String option) {
        return getOptionAsDouble(option, null);
    }

    @Nullable
    @Override
    public Double getOptionAsDouble(@NotNull String option, @Nullable Double defaultValue) {
        String optionAsString = getOptionAsString(option, String.valueOf(defaultValue));
        if (optionAsString == null) {
            return (Double) handleChecks(option, defaultValue);
        }
        try {
            return Double.parseDouble(optionAsString);
        } catch (NumberFormatException e) {
            return (Double) handleChecks(option, defaultValue);
        }
    }

    @Nullable
    @Override
    public Long getOptionAsLong(@NotNull String option) {
        return getOptionAsLong(option, null);
    }

    @Nullable
    @Override
    public Long getOptionAsLong(@NotNull String option, @Nullable Long defaultValue) {
        String optionAsString = getOptionAsString(option, String.valueOf(defaultValue));
        if (optionAsString == null) {
            return (Long) handleChecks(option, defaultValue);
        }
        try {
            return Long.parseLong(optionAsString);
        } catch (NumberFormatException e) {
            return (Long) handleChecks(option, defaultValue);
        }
    }

    @Nullable
    @Override
    public Boolean getOptionAsBoolean(@NotNull String option) {
        return getOptionAsBoolean(option, null);
    }

    @Nullable
    @Override
    public Boolean getOptionAsBoolean(@NotNull String option, @Nullable Boolean defaultValue) {
        String optionAsString = getOptionAsString(option, String.valueOf(defaultValue));
        if (optionAsString == null) {
            return (Boolean) handleChecks(option, defaultValue);
        }
        try {
            return Boolean.parseBoolean(optionAsString);
        } catch (NumberFormatException e) {
            return (Boolean) handleChecks(option, defaultValue);
        }
    }

    @Nullable
    @Override
    public User getOptionAsUser(@NotNull String option) {
        return getOptionAsUser(option, null);
    }

    @Nullable
    @Override
    public User getOptionAsUser(@NotNull String option, @Nullable User defaultValue) {
        String optionAsString = getOptionAsString(option, String.valueOf(defaultValue));
        if (optionAsString == null) {
            return (User) handleChecks(option, defaultValue);
        }
        User user;
        try {
            user = DiscordCache.getUserById(
                    optionAsString
                            .replace("<@", "")
                            .replace(">", "")
                            .replace("!", "")
            );
        } catch (Exception e) {
            return (User) handleChecks(option, defaultValue);
        }
        return user;
    }

    @Nullable
    @Override
    public Member getOptionAsMember(@NotNull String option) {
        return getOptionAsMember(option, null);
    }

    @Nullable
    @Override
    public Member getOptionAsMember(@NotNull String option, @Nullable Member defaultValue) {
        Guild guild = getGuild();
        if (!isFromGuild() || guild == null) {
            return defaultValue;
        }
        String optionAsString = getOptionAsString(option, String.valueOf(defaultValue));
        if (optionAsString == null) {
            return (Member) handleChecks(option, defaultValue);
        }
        Member member;
        try {
            member = guild.retrieveMemberById(
                    optionAsString
                            .replace("<@", "")
                            .replace(">", "")
                            .replace("!", "")
            ).complete();
        } catch (Exception e) {
            return (Member) handleChecks(option, defaultValue);
        }
        return member;
    }

    @Nullable
    @Override
    public Channel getOptionAsChannel(@NotNull String option) {
        return getOptionAsChannel(option, null);
    }

    @Nullable
    @Override
    public Channel getOptionAsChannel(@NotNull String option, @Nullable Channel defaultValue) {
        Guild guild = getGuild();
        if (!isFromGuild() || guild == null) {
            return defaultValue;
        }
        String optionAsString = getOptionAsString(option, String.valueOf(defaultValue));
        if (optionAsString == null) {
            return (Channel) handleChecks(option, defaultValue);
        }
        Channel channel;
        try {
            channel = guild.getTextChannelById(
                    optionAsString
                            .replace("<#", "")
                            .replace(">", "")
            );
        } catch (Exception e) {
            return (Channel) handleChecks(option, defaultValue);
        }
        return channel;
    }

    @Nullable
    @Override
    public Role getOptionAsRole(@NotNull String option) {
        return getOptionAsRole(option, null);
    }

    @Nullable
    @Override
    public Role getOptionAsRole(@NotNull String option, @Nullable Role defaultValue) {
        Guild guild = getGuild();
        if (!isFromGuild() || guild == null) {
            return defaultValue;
        }
        String optionAsString = getOptionAsString(option, String.valueOf(defaultValue));
        if (optionAsString == null) {
            return (Role) handleChecks(option, defaultValue);
        }
        Role role;
        try {
            role = guild.getRoleById(
                    optionAsString
                            .replace("<@", "")
                            .replace("&", "")
                            .replace(">", "")
            );
        } catch (Exception e) {
            return (Role) handleChecks(option, defaultValue);
        }
        return role;
    }

    @Nullable
    @Override
    public IMentionable getOptionAsMentionable(@NotNull String option) {
        return getOptionAsMentionable(option, null);
    }

    @Nullable
    @Override // Will remain undefined as it's not possible to know which mentionable type is expected.
    public IMentionable getOptionAsMentionable(@NotNull String option, @Nullable IMentionable defaultValue) {
        return null;
    }

    @Nullable
    @Override
    public Message.Attachment getOptionAsAttachment(@NotNull String option) {
        return getOptionAsAttachment(option, null);
    }

    @Nullable
    @Override // Will remain undefined for now.
    public Message.Attachment getOptionAsAttachment(@NotNull String option, @Nullable Message.Attachment defaultValue) {
        return null;
    }

    private Object handleChecks(String option, Object defaultValue) {
        if (isCommandOptionRequired(option)) {
            return null;
        }
        return defaultValue;
    }

    @NotNull
    @Override
    public CommandHolder<?> getCommandHolder() {
        return command;
    }

    @Override
    public boolean isUserIntegration() {
        return false;
    }

    @NotNull
    @Override
    public Message getMessage() {
        return event.getMessage();
    }

    @NotNull
    @Override
    public MessageChannel getMessageChannel() {
        return event.getChannel();
    }

    @Nullable
    @Override
    public Guild getGuild() {
        return event.getGuild();
    }

    @NotNull
    @Override
    public User getUser() {
        return event.getAuthor();
    }

    @Nullable
    @Override
    public Member getMember() {
        return event.getMember();
    }

}

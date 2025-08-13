package me.mrepiko.cymric.context.commands;

import me.mrepiko.cymric.context.plain.MessageChannelContext;
import me.mrepiko.cymric.elements.command.chat.GenericChatCommand;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ChatCommandContext extends CommandContext, MessageChannelContext {
    @NotNull
    GenericChatCommand getCommand();

    @Nullable
    String getOptionAsString(@NotNull String option);

    @Nullable
    String getOptionAsString(@NotNull String option, @Nullable String defaultValue);

    @Nullable
    String getOptionAsString(@NotNull String option, int pullArgs);

    @Nullable
    String getOptionAsString(@NotNull String option, int pullArgs, @Nullable String defaultValue);

    @Nullable
    Integer getOptionAsInteger(@NotNull String option);

    @Nullable
    Integer getOptionAsInteger(@NotNull String option, @Nullable Integer defaultValue);

    @Nullable
    Double getOptionAsDouble(@NotNull String option);

    @Nullable
    Double getOptionAsDouble(@NotNull String option, @Nullable Double defaultValue);

    @Nullable
    Long getOptionAsLong(@NotNull String option);

    @Nullable
    Long getOptionAsLong(@NotNull String option, @Nullable Long defaultValue);

    @Nullable
    Boolean getOptionAsBoolean(@NotNull String option);

    @Nullable
    Boolean getOptionAsBoolean(@NotNull String option, @Nullable Boolean defaultValue);
    
    @Nullable
    User getOptionAsUser(@NotNull String option);

    @Nullable
    User getOptionAsUser(@NotNull String option, @Nullable User defaultValue);
    
    @Nullable
    Member getOptionAsMember(@NotNull String option);

    @Nullable
    Member getOptionAsMember(@NotNull String option, @Nullable Member defaultValue);
    
    @Nullable
    Channel getOptionAsChannel(@NotNull String option);

    @Nullable
    Channel getOptionAsChannel(@NotNull String option, @Nullable Channel defaultValue);

    @Nullable
    Role getOptionAsRole(@NotNull String option);

    @Nullable
    Role getOptionAsRole(@NotNull String option, @Nullable Role defaultValue);

    @Nullable
    IMentionable getOptionAsMentionable(@NotNull String option);

    @Nullable
    IMentionable getOptionAsMentionable(@NotNull String option, @Nullable IMentionable defaultValue);

    @Nullable
    Message.Attachment getOptionAsAttachment(@NotNull String option);

    @Nullable
    Message.Attachment getOptionAsAttachment(@NotNull String option, @Nullable Message.Attachment defaultValue);
}

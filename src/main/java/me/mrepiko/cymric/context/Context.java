package me.mrepiko.cymric.context;

import me.mrepiko.cymric.DiscordBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Context {

    @Nullable
    User getUser();

    @Nullable
    Guild getGuild();

    @Nullable
    Member getMember();

    default boolean isFromGuild() {
        return getGuild() != null;
    }

    @NotNull
    default SelfUser getSelfUser() {
        return DiscordBot.getInstance().getFirstShard().getSelfUser();
    }

    @NotNull
    default JDA getShard() {
        return DiscordBot.getInstance().getFirstShard();
    }

    @NotNull
    default ShardManager getShardManager() {
        return DiscordBot.getInstance().getShardManager();
    }

}

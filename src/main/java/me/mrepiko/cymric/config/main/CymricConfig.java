package me.mrepiko.cymric.config.main;

import lombok.Getter;
import lombok.experimental.Delegate;
import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.config.ConfigFile;
import me.mrepiko.cymric.config.Configurable;
import me.mrepiko.cymric.jackson.JsonContainer;
import me.mrepiko.cymric.mics.Constants;
import me.mrepiko.okhttpwrapper.HttpMethod;
import me.mrepiko.okhttpwrapper.HttpRequest;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class CymricConfig implements Configurable {

    private final JsonContainer config;

    @Delegate
    private final CymricData data;

    public CymricConfig() {
        this.config = new JsonContainer(new ConfigFile(Constants.MAIN_CONFIG_FILE_PATH, true));
        this.data = config.getAsOrSetDefault(CymricData.class, new CymricData(), true);
    }

    @NotNull
    public DefaultShardManagerBuilder getBuilder() {
        String token = getToken();
        List<GatewayIntent> intents = getIntents();
        List<CacheFlag> cacheFlags = getCacheFlags();
        int shardCount = getShardCount();
        OnlineStatus status = getStatus();
        Activity.ActivityType activityType = getActivityType();
        String activityName = getActivityName();

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createLight(token, intents)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(
                        (intents.contains(GatewayIntent.GUILD_MEMBERS) || intents.contains(GatewayIntent.GUILD_PRESENCES))
                        ? MemberCachePolicy.ALL
                        : MemberCachePolicy.NONE
                )
                .enableCache(cacheFlags)
                .setBulkDeleteSplittingEnabled(false)
                .setShardsTotal((shardCount == 0) ? -1 : shardCount);

        if (status != null) {
            builder.setStatus(status);
        }
        if (activityType != null && activityName != null) {
            builder.setActivity(Activity.of(activityType, activityName));
        }
        return builder;
    }

    @NotNull
    public Guild getDevelopmentGuild() {
        String developmentGuildId = data.getDevelopmentGuildId();
        if (developmentGuildId == null || developmentGuildId.isEmpty()) {
            throw new IllegalStateException("Development guild ID is not set.");
        }
        Guild guild = DiscordBot.getInstance().getShardManager().getGuildById(developmentGuildId);
        if (guild == null) {
            throw new IllegalStateException("Development guild not found with ID: " + developmentGuildId);
        }
        return guild;
    }

    @NotNull
    public GuildMessageChannel getDevelopmentChannel() {
        String developmentChannelId = data.getDevelopmentChannelId();
        if (developmentChannelId == null || developmentChannelId.isEmpty()) {
            throw new IllegalStateException("Development channel ID is not set.");
        }
        TextChannel channel = getDevelopmentGuild().getTextChannelById(developmentChannelId);
        if (channel == null) {
            throw new IllegalStateException("Development channel not found with ID: " + developmentChannelId);
        }
        return channel;
    }

    @Nullable
    public EndpointData getEndpointData(@NotNull String endpointName) {
        return data.getEndpoints().get(endpointName);
    }

    @Nullable
    public HttpRequest.Builder getEndpointRequestBuilder(@NotNull String endpointName, @NotNull HttpMethod httpMethod) {
        EndpointData endpointData = getEndpointData(endpointName);
        if (endpointData == null) {
            return null;
        }
        String url = endpointData.getEndpointUrl();
        if (url == null || url.isEmpty()) {
            return null;
        }
        HttpRequest.Builder builder = HttpRequest.Builder.create(url, httpMethod);
        String authHeader = endpointData.getAuthorizationHeader();
        String authToken = endpointData.getAuthorizationToken();
        if (authHeader != null && authToken != null) {
            builder.addHeader(authHeader, authToken);
        }
        return builder;
    }

}

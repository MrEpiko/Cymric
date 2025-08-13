package me.mrepiko.cymric.config.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.*;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CymricData {

    private String token;
    private String prefix;
    private boolean development = true;
    private Map<String, Boolean> modules = Map.of("module_id", false);
    private int shardCount = -1;
    private List<GatewayIntent> intents = new ArrayList<>();
    private List<CacheFlag> cacheFlags = List.of(CacheFlag.MEMBER_OVERRIDES);
    private OnlineStatus status;
    private Activity.ActivityType activityType;
    private String activityName;
    private String presenceStatus;
    private String developmentGuildId;
    private String developmentChannelId;
    private List<String> botAdminIds = new ArrayList<>();
    private Map<String, String> constants = new HashMap<>();
    private String defaultDateFormat = "yyyy-MM-dd";
    private String defaultTimeFormat = "HH:mm:ss";
    private String truncationIndicator = "...";
    private Map<String, EndpointData> endpoints = Map.of("showcase", new EndpointData("https://showcase.com/", "Authorization", "12345"));

    private String choiceArgTemplate = "`{arg_name}` ({arg_required}) - {arg_type}\n- *{arg_description}*\n- Available choices: {arg_choices}\n\n";
    private String argTemplate = "`{arg_name}` ({arg_required}) - {arg_type}\n- *{arg_description}*\n\n";
    private String argRequired = "required";
    private String argOptional = "optional";

}

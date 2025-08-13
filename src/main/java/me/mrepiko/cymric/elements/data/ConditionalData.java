package me.mrepiko.cymric.elements.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;
import me.mrepiko.cymric.elements.ElementError;
import me.mrepiko.cymric.response.data.ResponseData;
import net.dv8tion.jda.api.Permission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConditionalData {

    @JsonAlias({"required_guilds", "required_guild"})
    private List<String> requiredGuildIds;
    @JsonAlias({"required_users", "required_user"})
    private List<String> requiredUserIds;
    @JsonAlias({"required_channels", "required_channel"})
    private List<String> requiredChannelIds;
    @JsonAlias({"required_roles", "required_role"})
    private List<String> requiredRoleIds;
    @JsonAlias({"required_invoker_permission"})
    private List<Permission> requiredInvokerPermissions;
    @JsonAlias({"required_bot_permission"})
    private List<Permission> requiredBotPermissions;
    private boolean talkRequired;
    private boolean botAdminRequired;
    private double cooldownMillis;
    private boolean enabled;
    private Map<ElementError, ResponseData> errorResponses = new HashMap<>();

}

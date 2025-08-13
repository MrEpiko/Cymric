package me.mrepiko.cymric.elements.command.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.mrepiko.cymric.elements.data.ConditionalData;
import me.mrepiko.cymric.mics.Utils;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommandData {

    private List<String> aliases;
    private boolean skipName;
    private List<String> registeredGuildIds;
    private CommandAvailabilityType availabilityType = CommandAvailabilityType.GUILD;
    private List<IntegrationType> integrationTypes = List.of(IntegrationType.GUILD_INSTALL);
    private boolean nsfw;

    @NotNull
    public List<String> getAllNames(@NotNull String originalName, @Nullable PlaceholderMap map) {
        List<String> aliases = getAliases();
        List<String> names = new ArrayList<>(aliases == null ? List.of() : aliases
                .stream()
                .map(alias -> Utils.applyPlaceholders(map, alias))
                .toList()
        );

        if (!isSkipName()) {
            names.add(Utils.applyPlaceholders(map, originalName));
        }

        return names;
    }

    public void syncCommandData(@NotNull JdaCommandData data, @NotNull ConditionalData conditionalData) {
        List<Permission> permissions = conditionalData.getRequiredInvokerPermissions();
        if (permissions != null && !permissions.isEmpty()) {
            data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(permissions));
        }

        data.setNSFW(nsfw);
        data.setIntegrationTypes(integrationTypes);
        data.setContexts(availabilityType.getInteractionContextTypes());
    }

}

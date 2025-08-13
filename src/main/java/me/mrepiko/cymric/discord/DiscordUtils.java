package me.mrepiko.cymric.discord;

import me.mrepiko.cymric.managers.runtime.RuntimeComponent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DiscordUtils {

    /**
     * Handles the disabling of components based on the provided parameters.
     * Upon disabling a component, it will appear as still enabled in the original Message object, hence why messageComponents are passed in.
     * Another reason for passing RuntimeComponents in general is to ensure users don't spam the same component which should be disabled.
     * This is also why RuntimeComponent has isEnabled method, which is used to check if the component is enabled or not.
     *
     * @param component         The RuntimeComponent.
     * @param messageComponents The list of all RuntimeComponents in the message.
     * @param disableAll        If true, disables all components in the message.
     * @param disable           If true, disables the specific component with the given uniqueElementId.
     */
    public static void handleComponentDisabling(
            @NotNull RuntimeComponent component,
            @NotNull List<RuntimeComponent> messageComponents,
            boolean disableAll,
            boolean disable
    ) {
        if (disableAll) {
            DiscordUtils.disableAllMessageComponents(component, messageComponents);
        } else if (disable) {
            DiscordUtils.disableMessageComponent(component, messageComponents);
        }
    }

    private static void disableAllMessageComponents(
            @NotNull RuntimeComponent runtimeComponent,
            @NotNull List<RuntimeComponent> messageComponents
    ) {
        Message message = runtimeComponent.getMessage();
        if (message == null) {
            return;
        }

        List<List<ActionComponent>> itemComponents = new ArrayList<>();
        for (LayoutComponent layout : message.getComponents()) {
            List<ActionComponent> actionComponents = new ArrayList<>();
            for (ActionComponent action : layout.getActionComponents()) {
                actionComponents.add(action.asDisabled());
                setRuntimeComponentAsDisabled(action, messageComponents);
            }
            itemComponents.add(actionComponents);
        }
        message.editMessageComponents(itemComponents.stream().map(ActionRow::of).toList()).queue();
    }

    private static void disableMessageComponent(
            @NotNull RuntimeComponent runtimeComponent,
            @NotNull List<RuntimeComponent> messageComponents
    ) {
        Message message = runtimeComponent.getMessage();
        String componentId = runtimeComponent.getUniqueElementId();
        if (message == null || componentId.isEmpty()) {
            return;
        }

        List<List<ActionComponent>> itemComponents = new ArrayList<>();
        for (LayoutComponent layout : message.getComponents()) {
            List<ActionComponent> actionComponents = new ArrayList<>();
            for (ActionComponent action : layout.getActionComponents()) {
                String id = action.getId();
                if (id == null) {
                    actionComponents.add(action);
                    continue;
                }
                if (!id.equalsIgnoreCase(componentId)) {
                    RuntimeComponent innerRuntimeComponent = getRuntimeComponent(action, messageComponents);
                    actionComponents.add(
                            (innerRuntimeComponent != null && !innerRuntimeComponent.isEnabled()) ?
                                    action.asDisabled() :
                                    action
                    );
                    continue;
                }
                actionComponents.add(action.asDisabled());
                setRuntimeComponentAsDisabled(action, messageComponents);
            }
            itemComponents.add(actionComponents);
        }
        message.editMessageComponents(itemComponents.stream().map(ActionRow::of).toList()).queue();
    }

    @Nullable
    private static RuntimeComponent getRuntimeComponent(
            @NotNull ActionComponent actionComponent,
            @NotNull List<RuntimeComponent> messageComponents
    ) {
        for (RuntimeComponent component : messageComponents) {
            String id = component.getActionComponent().getId();
            if (id == null) {
                continue;
            }
            if (id.equalsIgnoreCase(actionComponent.getId())) {
                return component;
            }
        }
        return null;
    }

    private static void setRuntimeComponentAsDisabled(
            @NotNull ActionComponent actionComponent,
            @NotNull List<RuntimeComponent> messageComponents
    ) {
        RuntimeComponent runtimeComponent = getRuntimeComponent(actionComponent, messageComponents);
        if (runtimeComponent == null) {
            return;
        }
        runtimeComponent.setEnabled(false);
    }

    /**
     * Validates if the given string is a valid Discord snowflake ID.
     *
     * @param input The string to validate.
     * @return true if the string is a valid snowflake ID, false otherwise.
     */
    public static boolean isValidSnowflake(@NotNull String input) {
        try {
            long id = Long.parseUnsignedLong(input);
            return id > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }


}

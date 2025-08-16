package me.mrepiko.cymric.managers.impl;

import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.annotations.elements.CymricComponent;
import me.mrepiko.cymric.context.components.ButtonContext;
import me.mrepiko.cymric.context.components.ComponentContext;
import me.mrepiko.cymric.context.components.EntitySelectMenuContext;
import me.mrepiko.cymric.context.components.StringSelectMenuContext;
import me.mrepiko.cymric.context.components.impl.ButtonContextImpl;
import me.mrepiko.cymric.context.components.impl.EntitySelectMenuContextImpl;
import me.mrepiko.cymric.context.components.impl.StringSelectMenuContextImpl;
import me.mrepiko.cymric.discord.DiscordUtils;
import me.mrepiko.cymric.elements.DeferType;
import me.mrepiko.cymric.elements.components.ComponentLoader;
import me.mrepiko.cymric.elements.components.ForgedComponentDataContainer;
import me.mrepiko.cymric.elements.components.button.GenericButton;
import me.mrepiko.cymric.elements.components.button.data.ForgedButtonData;
import me.mrepiko.cymric.elements.components.selectmenus.entityselect.GenericEntitySelectMenu;
import me.mrepiko.cymric.elements.components.selectmenus.entityselect.data.ForgedEntitySelectMenuData;
import me.mrepiko.cymric.elements.components.selectmenus.stringselect.GenericStringSelectMenu;
import me.mrepiko.cymric.elements.components.selectmenus.stringselect.data.ForgedStringSelectMenuData;
import me.mrepiko.cymric.elements.data.ComponentData;
import me.mrepiko.cymric.managers.ComponentManager;
import me.mrepiko.cymric.managers.GenericElementManager;
import me.mrepiko.cymric.managers.runtime.RuntimeComponent;
import me.mrepiko.cymric.managers.runtime.RuntimeExtra;
import me.mrepiko.cymric.managers.runtime.impl.RuntimeComponentImpl;
import me.mrepiko.cymric.mics.Constants;
import me.mrepiko.cymric.mics.Utils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ComponentManagerImpl extends GenericElementManager<ComponentLoader<?>> implements ComponentManager {

    // Unique element ID: RuntimeComponent
    // ID above refers to element ID with mix of characters at the end
    private final Map<String, RuntimeComponent> runtimeComponents = new HashMap<>();

    private final List<String> DIRECTORY_PATHS = List.of(
            Constants.BUTTON_CONFIGURATION_FOLDER_PATH,
            Constants.STRING_SELECT_MENU_CONFIGURATION_FOLDER_PATH,
            Constants.ENTITY_SELECT_MENU_CONFIGURATION_FOLDER_PATH
    );

    private final List<Class<? extends ComponentLoader<?>>> componentTypes = List.of(
            GenericButton.class,
            GenericStringSelectMenu.class,
            GenericEntitySelectMenu.class
    );

    public ComponentManagerImpl() {
        DiscordBot.getInstance().getShardManager().addEventListener(this);
    }

    @Override
    public void addRuntimeComponent(@NotNull RuntimeComponent runtimeComponent) {
        runtimeComponents.put(runtimeComponent.getUniqueElementId(), runtimeComponent);
    }

    @Nullable
    @Override
    public RuntimeComponent getRuntimeComponent(@NotNull String uniqueElementId) {
        return runtimeComponents.get(uniqueElementId);
    }

    @NotNull
    @Override
    public List<RuntimeComponent> getRuntimeComponents(@NotNull Message message) {
        List<RuntimeComponent> components = new ArrayList<>();
        for (RuntimeComponent value : runtimeComponents.values()) {
            Message valueMessage = value.getMessage();
            if (valueMessage != null && valueMessage.getId().equals(message.getId())) {
                components.add(value);
            }
        }
        return components;
    }

    private RuntimeComponent createRuntimeComponent(@Nullable RuntimeComponent runtimeComponent, @NotNull User creator, @NotNull ComponentLoader<?> componentHolder, @NotNull ForgedComponentDataContainer overriddenData, @NotNull ActionComponent actionComponent) {
        if (runtimeComponent != null) {
            return runtimeComponent;
        }
        return new RuntimeComponentImpl(
                creator,
                componentHolder,
                overriddenData,
                actionComponent,
                new RuntimeExtra(),
                null
        );
    }

    @Override
    public void register() {
        for (String path : DIRECTORY_PATHS) {
            setupDirectory(path);
        }
        for (Class<? extends ComponentLoader<?>> type : componentTypes) {
            register(CymricComponent.class, type);
        }
    }

    // Interactions

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String uniqueComponentId = event.getComponentId();
        String sanitizedComponentId = Utils.getSanitizedComponentId(uniqueComponentId);

        GenericButton genericButton = (GenericButton) getById(sanitizedComponentId);
        RuntimeComponent runtimeComponent = getRuntimeComponent(uniqueComponentId);
        ForgedButtonData data = runtimeComponent == null ? genericButton.getData() : (ForgedButtonData) runtimeComponent.getOverriddenData();

        runtimeComponent = createRuntimeComponent(
                runtimeComponent,
                event.getUser(),
                genericButton,
                data,
                event.getButton()
        );

        ButtonContext context = new ButtonContextImpl(event, runtimeComponent);
        if (!handleComponentInteraction(event, genericButton, data, context, runtimeComponent)) {
            return;
        }

        genericButton.onInteraction(context);
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        String uniqueComponentId = event.getComponentId();
        String sanitizedComponentId = Utils.getSanitizedComponentId(uniqueComponentId);

        GenericStringSelectMenu genericSelectMenu = (GenericStringSelectMenu) getById(sanitizedComponentId);
        RuntimeComponent runtimeComponent = getRuntimeComponent(uniqueComponentId);
        ForgedStringSelectMenuData data = (runtimeComponent == null) ? genericSelectMenu.getData() : ((GenericStringSelectMenu) runtimeComponent.getElement()).getData();

        runtimeComponent = createRuntimeComponent(
                runtimeComponent,
                event.getUser(),
                genericSelectMenu,
                data,
                event.getSelectMenu()
        );

        StringSelectMenuContext context = new StringSelectMenuContextImpl(event, runtimeComponent);
        if (!handleComponentInteraction(event, genericSelectMenu, data, context, runtimeComponent)) {
            return;
        }

        genericSelectMenu.onInteraction(context);
    }

    @Override
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
        String uniqueComponentId = event.getComponentId();
        String sanitizedComponentId = Utils.getSanitizedComponentId(uniqueComponentId);

        GenericEntitySelectMenu genericSelectMenu = (GenericEntitySelectMenu) getById(sanitizedComponentId);
        RuntimeComponent runtimeComponent = getRuntimeComponent(uniqueComponentId);
        ForgedEntitySelectMenuData data = (runtimeComponent == null) ? genericSelectMenu.getData() : ((GenericEntitySelectMenu) runtimeComponent.getElement()).getData();

        runtimeComponent = createRuntimeComponent(
                runtimeComponent,
                event.getUser(),
                genericSelectMenu,
                data,
                event.getSelectMenu()
        );

        EntitySelectMenuContext context = new EntitySelectMenuContextImpl(event, runtimeComponent);
        if (!handleComponentInteraction(event, genericSelectMenu, data, context, runtimeComponent)) {
            return;
        }

        genericSelectMenu.onInteraction(context);
    }

    // Miscellaneous

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private <T extends ComponentContext> boolean handleComponentInteraction(
            @NotNull GenericComponentInteractionCreateEvent event,
            @NotNull ComponentLoader<?> holder,
            @NotNull ForgedComponentDataContainer data,
            @NotNull T context,
            @NotNull RuntimeComponent runtimeComponent
    ) {
        if (!runtimeComponent.isEnabled()) {
            event.reply("This component has been disabled.").setEphemeral(true).queue();
            return false;
        }

        if (isCreatorInvalid(holder, runtimeComponent, event.getUser()) || !holder.check(context, data.getConditionalData())) {
            return false;
        }

        handleDefer(data.getDeferrableElementData().getDeferType(), event);
        if (runtimeComponent.getInteractionOverride() != null) {
            runtimeComponent.getInteractionOverride().accept(context);
            return false;
        }

        holder.setUserCooldown(event.getUser(), data.getConditionalData());
        ComponentData componentData = data.getComponentData();
        DiscordUtils.handleComponentDisabling(
                runtimeComponent,
                getRuntimeComponents(event.getMessage()),
                componentData.isDisableAllOnceUsed(),
                componentData.isDisableOnceUsed()
        );
        return true;
    }

    private boolean isCreatorInvalid(ComponentLoader<?> holder, @Nullable RuntimeComponent runtimeComponent, User invoker) {
        if (!holder.getComponentData().isCreatorOnly()) {
            return false;
        }
        if (runtimeComponent == null) {
            return true;
        }
        User creator = runtimeComponent.getCreator();
        return !creator.getId().equalsIgnoreCase(invoker.getId());
    }

    private void handleDefer(@NotNull DeferType deferType, @NotNull GenericComponentInteractionCreateEvent event) {
        switch (deferType) {
            case ENDURING -> event.deferReply().queue();
            case EPHEMERAL -> event.deferReply(true).queue();
            case EDIT -> event.deferEdit().queue();
        }
    }

}

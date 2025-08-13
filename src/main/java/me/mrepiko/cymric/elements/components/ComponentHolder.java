package me.mrepiko.cymric.elements.components;

import lombok.Getter;
import lombok.Setter;
import me.mrepiko.cymric.CymricApi;
import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.context.plain.MessageContext;
import me.mrepiko.cymric.discord.DiscordUtils;
import me.mrepiko.cymric.elements.ConditionalHolder;
import me.mrepiko.cymric.elements.data.ComponentData;
import me.mrepiko.cymric.elements.data.TimeoutableElementData;
import me.mrepiko.cymric.elements.plain.ComponentTemplate;
import me.mrepiko.cymric.elements.plain.SerializableBotElement;
import me.mrepiko.cymric.managers.runtime.RuntimeComponent;
import me.mrepiko.cymric.placeholders.PlaceholderMap;
import me.mrepiko.cymric.response.ResponseBuilder;
import me.mrepiko.cymric.response.data.ResponseData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public abstract class ComponentHolder<T> extends ConditionalHolder implements ComponentTemplate, SerializableBotElement<T> {

    private final CymricApi instance = DiscordBot.getInstance();

    private TimeoutableElementData timeoutableData;
    private ComponentData componentData;

    @Override
    public void onTimeout(@NotNull RuntimeComponent runtimeComponent, @NotNull MessageContext context) {
        ForgedComponentDataContainer overriddenData = runtimeComponent.getOverriddenData();
        TimeoutableElementData timeoutableData = overriddenData.getTimeoutableElementData();

        ResponseData responseData = timeoutableData.getTimeoutResponseData();
        if (responseData != null) {
            ResponseBuilder.create(context.getPlaceholderMap(), responseData).buildAndSend();
        }

        DiscordUtils.handleComponentDisabling(
                runtimeComponent,
                instance.getComponentManager().getRuntimeComponents(context.getMessage()),
                timeoutableData.isDisableAllOnceTimedOut(),
                timeoutableData.isDisableOnceTimedOut()
        );
    }

    @NotNull
    public abstract RowComponent getRowComponent(@NotNull PlaceholderMap map, @Nullable Object overriddenData);

}

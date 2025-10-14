package me.mrepiko.cymric.elements.managers.runtime;

import me.mrepiko.cymric.context.plain.MessageChannelContext;
import me.mrepiko.cymric.elements.plain.BotElement;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

// Generic interface that represents an element that is currently being interacted with in the runtime.
// It's generally used for components and modals and can hold extra data as well as custom interaction overrides and component and modal appearances.
public interface RuntimeElement<T extends BotElement, D, U extends MessageChannelContext> {
    @NotNull
    String getUniqueElementId();

    @NotNull
    T getElement();

    @NotNull
    D getOverriddenData();

    @Nullable
    Consumer<U> getInteractionOverride();

    @NotNull
    User getCreator();

    @NotNull
    RuntimeExtra getExtra();

    @Nullable
    Message getMessage();
}

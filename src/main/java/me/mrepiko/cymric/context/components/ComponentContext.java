package me.mrepiko.cymric.context.components;

import me.mrepiko.cymric.context.ReplyCallbackContext;
import me.mrepiko.cymric.context.plain.MessageContext;
import me.mrepiko.cymric.elements.components.ComponentHolder;
import me.mrepiko.cymric.managers.runtime.RuntimeComponent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import org.jetbrains.annotations.NotNull;

public interface ComponentContext extends MessageContext, ReplyCallbackContext {
    @NotNull
    RuntimeComponent getRuntimeComponent();

    @NotNull
    ComponentInteraction getComponentInteraction();

    @NotNull
    ComponentHolder<?> getComponentHolder();

    @NotNull
    @Override
    default IReplyCallback getReplyCallback() {
        return getComponentInteraction();
    }
}

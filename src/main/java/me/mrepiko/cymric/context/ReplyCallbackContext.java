package me.mrepiko.cymric.context;

import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.jetbrains.annotations.NotNull;

public interface ReplyCallbackContext {
    @NotNull
    IReplyCallback getReplyCallback();
}

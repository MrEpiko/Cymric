package me.mrepiko.cymric.context.plain;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public interface MessageContext extends MessageChannelContext {
    @NotNull
    Message getMessage();

    @Override
    @NotNull
    User getUser();
}

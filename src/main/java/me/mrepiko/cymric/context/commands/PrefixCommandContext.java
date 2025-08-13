package me.mrepiko.cymric.context.commands;

import me.mrepiko.cymric.context.plain.MessageContext;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PrefixCommandContext extends ChatCommandContext, MessageContext {
    @NotNull
    MessageReceivedEvent getEvent();

    @NotNull
    List<String> getArgs();

    void resetArgIndex();
}

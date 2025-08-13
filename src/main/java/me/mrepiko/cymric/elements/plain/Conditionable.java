package me.mrepiko.cymric.elements.plain;

import me.mrepiko.cymric.context.plain.MessageChannelContext;
import me.mrepiko.cymric.elements.data.ConditionalData;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public interface Conditionable {
    boolean check(@NotNull MessageChannelContext context);

    boolean check(@NotNull MessageChannelContext context, @NotNull ConditionalData overriddenData);

    void setUserCooldown(@NotNull User user, @NotNull ConditionalData conditionalData);

    void setUserCooldown(@NotNull User user);
}

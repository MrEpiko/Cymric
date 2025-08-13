package me.mrepiko.cymric.elements.plain;

import me.mrepiko.cymric.config.Configurable;
import org.jetbrains.annotations.NotNull;

public interface SerializableBotElement<T> extends BotElement, Configurable, Reloadable {
    @NotNull
    T getData();

    /**
     * Indicates that either configuration file is missing or configuration is incomplete.
     */
    boolean isConfigMissing();
}

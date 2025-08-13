package me.mrepiko.cymric.response;

import me.mrepiko.cymric.placeholders.PlaceholderMap;
import org.jetbrains.annotations.NotNull;

public interface Response {
    @NotNull
    PlaceholderMap getMap();

    @NotNull
    Action getAction();

    @NotNull
    ResponseChain getResponseChain();

    void send();
}

package me.mrepiko.cymric.placeholders;

import org.jetbrains.annotations.NotNull;

public interface Placeholderable {
    void appendToPlaceholderMap(@NotNull String identifier, @NotNull PlaceholderMap mapToAppendTo);
}

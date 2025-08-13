package me.mrepiko.cymric.elements.containers;

import me.mrepiko.cymric.elements.data.TimeoutableElementData;
import org.jetbrains.annotations.NotNull;

public interface TimeoutableDataContainer {
    @NotNull
    TimeoutableElementData getTimeoutableElementData();
}

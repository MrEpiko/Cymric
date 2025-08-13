package me.mrepiko.cymric.elements.containers;

import me.mrepiko.cymric.elements.data.ConditionalData;
import org.jetbrains.annotations.NotNull;

public interface ConditionalDataContainer {
    @NotNull
    ConditionalData getConditionalData();
}

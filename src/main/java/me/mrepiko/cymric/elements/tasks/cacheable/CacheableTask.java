package me.mrepiko.cymric.elements.tasks.cacheable;

import me.mrepiko.cymric.elements.tasks.Task;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface CacheableTask<K, V> extends Task {
    @NotNull
    Optional<V> getCachedItem(@NotNull K key);

    void addToCache(@NotNull K key, @NotNull V item);
}

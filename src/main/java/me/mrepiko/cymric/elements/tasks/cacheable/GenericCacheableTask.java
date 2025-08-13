package me.mrepiko.cymric.elements.tasks.cacheable;

import lombok.Getter;
import me.mrepiko.cymric.elements.tasks.GenericTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class GenericCacheableTask<K, V> extends GenericTask implements CacheableTask<K, V> {

    // K (usually an ID): V
    private final Map<K, V> cache = new HashMap<>();

    public GenericCacheableTask(@NotNull String id, double interval, double period, @NotNull TimeUnit timeUnit) {
        super(id, interval, period, timeUnit);
    }

    @NotNull
    @Override
    public Optional<V> getCachedItem(@NotNull K key) {
        if (cache.containsKey(key)) {
            return Optional.of(cache.get(key));
        }
        return Optional.empty();
    }

    @Override
    public void addToCache(@NotNull K key, @NotNull V item) {
        cache.put(key, item);
    }

    @Override
    public abstract void run();

}

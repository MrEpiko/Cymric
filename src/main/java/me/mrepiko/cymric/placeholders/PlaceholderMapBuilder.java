package me.mrepiko.cymric.placeholders;

import me.mrepiko.cymric.context.plain.MessageChannelContext;
import org.jetbrains.annotations.Nullable;

public class PlaceholderMapBuilder {

    @Nullable
    private final MessageChannelContext context;

    @Nullable
    private final PlaceholderMap map;

    private boolean includeContextPlaceholders = true;
    private boolean includeConstantPlaceholders = true;
    private boolean includeCommandPlaceholders = false;

    private PlaceholderMapBuilder(@Nullable MessageChannelContext context, @Nullable PlaceholderMap map) {
        this.context = context;
        this.map = map;
    }

    public static PlaceholderMapBuilder create(@Nullable MessageChannelContext context) {
        return new PlaceholderMapBuilder(context, null);
    }

    public static PlaceholderMapBuilder create(@Nullable MessageChannelContext context, @Nullable PlaceholderMap overrideMap) {
        return new PlaceholderMapBuilder(context, overrideMap);
    }

    public static PlaceholderMapBuilder create() {
        return new PlaceholderMapBuilder(null, null);
    }

    public PlaceholderMapBuilder includeContextPlaceholders(boolean includeContextPlaceholders) {
        this.includeContextPlaceholders = includeContextPlaceholders;
        return this;
    }

    public PlaceholderMapBuilder includeConstantPlaceholders(boolean includeConstantPlaceholders) {
        this.includeConstantPlaceholders = includeConstantPlaceholders;
        return this;
    }

    public PlaceholderMapBuilder includeCommandPlaceholders(boolean includeCommandPlaceholders) {
        this.includeCommandPlaceholders = includeCommandPlaceholders;
        return this;
    }

    public PlaceholderMap build() {
        return new PlaceholderMapImpl(context, includeContextPlaceholders, includeConstantPlaceholders, includeCommandPlaceholders, map);
    }

}

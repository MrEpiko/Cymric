package me.mrepiko.cymric.managers;

import me.mrepiko.cymric.elements.plain.BotElement;
import me.mrepiko.cymric.elements.plain.Reloadable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.List;

public interface ElementManager<T extends BotElement> extends Reloadable {
    @NotNull
    File setupDirectory(@NotNull String directoryPath);

    void register();

    void register(@NotNull Class<? extends Annotation> annotation, @Nullable Class<? extends T> type);

    // All elements within provided dir will have functionality of the provided type.
    <F extends T> void register(@NotNull File dir, @NotNull Class<F> type);

    void register(@NotNull T element);

    @NotNull
    T getById(@NotNull String id);

    @NotNull
    T getByClass(@NotNull Class<? extends T> clazz);

    @NotNull
    List<T> getRegistered();
}

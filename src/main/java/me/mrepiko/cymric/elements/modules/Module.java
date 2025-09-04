package me.mrepiko.cymric.elements.modules;

import me.mrepiko.cymric.config.Configurable;
import me.mrepiko.cymric.elements.plain.BotElement;
import me.mrepiko.cymric.elements.plain.Reloadable;
import me.mrepiko.cymric.elements.tasks.Task;
import me.mrepiko.cymric.jackson.JsonContainer;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Module extends BotElement, Configurable, Reloadable {
    @NotNull
    String getId();

    boolean isConfigurable();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    @Nullable
    @Override
    JsonContainer getConfig();

    @NotNull
    List<ListenerAdapter> getListeners();

    @NotNull
    List<Task> getTasks();

    void registerListener(@NotNull ListenerAdapter listener);

    void registerTask(@NotNull Task task);

    void registerAndStartTask(@NotNull Task task);

    void onEnable();
}

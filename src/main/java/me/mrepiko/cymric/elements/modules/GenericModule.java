package me.mrepiko.cymric.elements.modules;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.mrepiko.cymric.CymricApi;
import me.mrepiko.cymric.DiscordBot;
import me.mrepiko.cymric.config.ConfigFile;
import me.mrepiko.cymric.elements.tasks.Task;
import me.mrepiko.cymric.jackson.JacksonUtils;
import me.mrepiko.cymric.jackson.JsonContainer;
import me.mrepiko.cymric.mics.Constants;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class GenericModule implements Module {

    private final CymricApi instance = DiscordBot.getInstance();

    @Getter(AccessLevel.NONE)
    private final String filePath;

    private final String id;
    private final boolean configurable;

    @Setter
    private boolean enabled = false;

    private final List<ListenerAdapter> listeners = new ArrayList<>();
    private final List<Task> tasks = new ArrayList<>();

    @Getter
    @Nullable
    private JsonContainer config;

    public GenericModule(@NotNull String id, boolean configurable) {
        this.id = id;
        this.configurable = configurable;
        this.filePath = Constants.MODULE_CONFIGURATION_FOLDER_PATH + id + ".json";
    }

    @Override
    public void reload() {
        if (!configurable) {
            return;
        }
        this.config = new JsonContainer(new ConfigFile(this.filePath, true));
        JacksonUtils.mergeDeclaredFieldsFromJson(this, config);
    }

    @Override
    public void registerListener(@NotNull ListenerAdapter listener) {
        listeners.add(listener);
        instance.getShardManager().addEventListener(listener);
    }

    @Override
    public void registerTask(@NotNull Task task) {
        tasks.add(task);
        instance.getTaskManager().register(task);
    }

    @Override
    public void registerAndStartTask(@NotNull Task task) {
        registerTask(task);
        task.run();
    }

    @Override
    public abstract void onEnable();
}

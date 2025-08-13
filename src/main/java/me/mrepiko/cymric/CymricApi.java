package me.mrepiko.cymric;

import me.mrepiko.cymric.config.defaultobject.DefaultObjectConfig;
import me.mrepiko.cymric.config.main.CymricConfig;
import me.mrepiko.cymric.elements.plain.Rebootable;
import me.mrepiko.cymric.elements.plain.Reloadable;
import me.mrepiko.cymric.managers.*;
import me.mrepiko.cymric.mics.EventWaiter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

public interface CymricApi extends Rebootable, Reloadable {

    Reflections reflections = new Reflections(CymricApi.class.getPackageName());

    @NotNull
    EventWaiter getEventWaiter();

    @NotNull
    ShardManager getShardManager();

    @NotNull
    CommandManager getCommandManager();

    @NotNull
    ComponentManager getComponentManager();

    @NotNull
    ModalManager getModalManager();

    @NotNull
    ModuleManager getModuleManager();

    @NotNull
    TaskManager getTaskManager();

    @NotNull
    JDA getFirstShard();

    @NotNull
    CymricConfig getConfig();

    @NotNull
    DefaultObjectConfig getDefaultObjectConfig();

    void start();
}

package me.mrepiko.cymric.managers;

import me.mrepiko.cymric.elements.plain.Rebootable;
import me.mrepiko.cymric.elements.tasks.GenericTask;
import org.jetbrains.annotations.NotNull;

public interface TaskManager extends ElementManager<GenericTask>, Rebootable {

    void startAllTasks();

    void stopAllTasks();

    void register(@NotNull GenericTask task);

    void registerAndStart(@NotNull GenericTask task);
}

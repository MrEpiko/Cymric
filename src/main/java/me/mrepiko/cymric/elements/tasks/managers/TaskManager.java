package me.mrepiko.cymric.elements.tasks.managers;

import me.mrepiko.cymric.elements.managers.ElementManager;
import me.mrepiko.cymric.elements.plain.Rebootable;
import me.mrepiko.cymric.elements.tasks.Task;
import org.jetbrains.annotations.NotNull;

public interface TaskManager extends ElementManager<Task>, Rebootable {

    void startAllTasks();

    void stopAllTasks();

    void register(@NotNull Task task);

    void registerAndStart(@NotNull Task task);
}

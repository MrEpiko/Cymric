package me.mrepiko.cymric.elements.tasks.managers;

import me.mrepiko.cymric.annotations.elements.CymricTask;
import me.mrepiko.cymric.elements.tasks.Task;
import me.mrepiko.cymric.elements.managers.GenericElementManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class TaskManagerImpl extends GenericElementManager<Task> implements TaskManager {

    private final ScheduledExecutorService service = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);

    @Override
    public void register() {
        register(CymricTask.class, Task.class);
    }

    @Override
    public void registerAndStart(@NotNull Task task) {
        register(task);
        scheduleTask(task);
    }

    @Override
    public void startAllTasks() {
        for (Task value : elements.values()) {
            scheduleTask(value);
        }
    }

    @Override
    public void stopAllTasks() {
        for (Task value : elements.values()) {
            value.stop();
        }
    }

    @Override
    public void reboot() {
        for (Task task : elements.values()) {
            if (!task.isCallUponReboot()) {
                continue;
            }
            task.stop();
            task.run();
        }
    }

    private void scheduleTask(@NotNull Task task) {
        ScheduledFuture<?> scheduledFuture;
        switch (task.getType()) {
            case SCHEDULED -> {
                scheduledFuture = service.schedule(task, (long) task.getInterval(), task.getTimeUnit());
                service.schedule(() -> {
                    elements.remove(task.getId());
                }, (long) task.getInterval() + 3, task.getTimeUnit()); // Remove task after it's completed.
            }
            case REPEATING -> {
                scheduledFuture = service.scheduleAtFixedRate(task, (long) task.getInterval(), (long) task.getPeriod(), task.getTimeUnit());
            }
            default -> {
                throw new IllegalArgumentException("Unknown task type: " + task.getType());
            }
        }
        task.setFuture(scheduledFuture);
        task.setStartedAtTimestamp(System.currentTimeMillis() / 1000);
    }
}

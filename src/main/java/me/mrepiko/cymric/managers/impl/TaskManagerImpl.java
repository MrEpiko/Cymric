package me.mrepiko.cymric.managers.impl;

import me.mrepiko.cymric.annotations.elements.CymricTask;
import me.mrepiko.cymric.elements.tasks.GenericTask;
import me.mrepiko.cymric.elements.tasks.cacheable.GenericCacheableTask;
import me.mrepiko.cymric.managers.GenericElementManager;
import me.mrepiko.cymric.managers.TaskManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class TaskManagerImpl extends GenericElementManager<GenericTask> implements TaskManager {

    private final ScheduledExecutorService service = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);

    @Override
    public void register() {
        register(CymricTask.class, GenericTask.class);
    }

    @Override
    public void registerAndStart(@NotNull GenericTask task) {
        register(task);
        scheduleTask(task);
    }

    @Override
    public void executeAllCacheableTasks() {
        for (GenericTask value : elements.values()) {
            if (!(value instanceof GenericCacheableTask<?,?> cacheable)) {
                continue;
            }
            cacheable.run();
        }
    }

    @Override
    public void startAllTasks() {
        for (GenericTask value : elements.values()) {
            scheduleTask(value);
        }
    }

    @Override
    public void stopAllTasks() {
        for (GenericTask value : elements.values()) {
            value.stop();
        }
    }

    private void scheduleTask(@NotNull GenericTask task) {
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

package me.mrepiko.cymric.elements.tasks;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class GenericTask implements Task {

    private final String id;
    private final double interval;
    private final double period;
    private final TimeUnit timeUnit;
    private final TaskType type;
    private final boolean callUponReboot;

    @Setter
    private long startedAtTimestamp;
    @Setter
    private ScheduledFuture<?> future;

    public GenericTask(@NotNull String id, double interval, double period, boolean callUponReboot, @NotNull TimeUnit timeUnit) {
        this.id = id;
        this.interval = interval;
        this.callUponReboot = callUponReboot;
        this.timeUnit = timeUnit;
        this.period = period;
        this.type = TaskType.REPEATING;
    }

    public GenericTask(@NotNull String id, double interval, boolean callUponReboot, @NotNull TimeUnit timeUnit) {
        this.id = id;
        this.callUponReboot = callUponReboot;
        this.interval = interval;
        this.timeUnit = timeUnit;
        this.period = 0;
        this.type = TaskType.SCHEDULED;
    }

    @Override
    public void stop() {
        this.future.cancel(true);
    }

    @Override
    public abstract void run();

}

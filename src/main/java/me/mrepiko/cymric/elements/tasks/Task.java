package me.mrepiko.cymric.elements.tasks;

import me.mrepiko.cymric.elements.plain.BotElement;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface Task extends Runnable, BotElement {
    double getInterval();

    double getPeriod();

    @NotNull
    TimeUnit getTimeUnit();

    @NotNull
    TaskType getType();

    long getStartedAtTimestamp();

    void setStartedAtTimestamp(long timestamp);

    boolean isCallUponReboot();

    @NotNull
    ScheduledFuture<?> getFuture();

    void setFuture(@NotNull ScheduledFuture<?> future);

    void stop();
}

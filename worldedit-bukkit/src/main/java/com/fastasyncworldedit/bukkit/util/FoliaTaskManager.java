package com.fastasyncworldedit.bukkit.util;

import com.fastasyncworldedit.core.util.TaskManager;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FoliaTaskManager extends TaskManager {

    private final Plugin plugin;
    private final GlobalRegionScheduler globalScheduler;
    private final AsyncScheduler asyncScheduler;
    private final AtomicInteger taskIdCounter = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, ScheduledTask> tasks = new ConcurrentHashMap<>();

    public FoliaTaskManager(final Plugin plugin) {
        this.plugin = plugin;
        this.globalScheduler = Bukkit.getGlobalRegionScheduler();
        this.asyncScheduler = Bukkit.getAsyncScheduler();
    }

    @Override
    public int repeat(@Nonnull final Runnable runnable, final int interval) {
        final int taskId = taskIdCounter.incrementAndGet();
        final ScheduledTask scheduledTask = globalScheduler.runAtFixedRate(plugin, (task) -> {
            runnable.run();
        }, 1, interval);
        tasks.put(taskId, scheduledTask);
        return taskId;
    }

    @Override
    public int repeatAsync(@Nonnull final Runnable runnable, final int interval) {
        final int taskId = taskIdCounter.incrementAndGet();
        final long period = interval * 50L;
        final ScheduledTask scheduledTask = asyncScheduler.runAtFixedRate(plugin, (task) -> {
            runnable.run();
        }, 0, period, TimeUnit.MILLISECONDS);
        tasks.put(taskId, scheduledTask);
        return taskId;
    }

    @Override
    public void async(@Nonnull final Runnable runnable) {
        asyncScheduler.runNow(plugin, (task) -> runnable.run());
    }

    @Override
    public void task(@Nonnull final Runnable runnable) {
        globalScheduler.run(plugin, (task) -> runnable.run());
    }

    @Override
    public void later(@Nonnull final Runnable runnable, final int delay) {
        if (delay <= 0) {
            globalScheduler.run(plugin, (task) -> runnable.run());
        } else {
            globalScheduler.runDelayed(plugin, (task) -> runnable.run(), delay);
        }
    }

    @Override
    public void laterAsync(@Nonnull final Runnable runnable, final int delay) {
        if (delay <= 0) {
            asyncScheduler.runNow(plugin, (task) -> runnable.run());
        } else {
            final long delayMs = delay * 50L;
            asyncScheduler.runDelayed(plugin, (task) -> runnable.run(), delayMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void cancel(final int task) {
        if (task != -1) {
            final ScheduledTask scheduledTask = tasks.remove(task);
            if (scheduledTask != null) {
                scheduledTask.cancel();
            }
        }
    }

    public void taskAtLocation(@Nonnull final Location location, @Nonnull final Runnable runnable) {
        final RegionScheduler regionScheduler = Bukkit.getRegionScheduler();
        regionScheduler.run(plugin, location, (task) -> runnable.run());
    }

    public void taskAtLocationLater(@Nonnull final Location location, @Nonnull final Runnable runnable, final int delay) {
        final RegionScheduler regionScheduler = Bukkit.getRegionScheduler();
        if (delay <= 0) {
            regionScheduler.run(plugin, location, (task) -> runnable.run());
        } else {
            regionScheduler.runDelayed(plugin, location, (task) -> runnable.run(), delay);
        }
    }

    public void taskAtLocationRepeat(@Nonnull final Location location, @Nonnull final Runnable runnable, final int interval) {
        final RegionScheduler regionScheduler = Bukkit.getRegionScheduler();
        regionScheduler.runAtFixedRate(plugin, location, (task) -> runnable.run(), 1, interval);
    }

}


package com.allthegoodies.util;

import com.allthegoodies.AllTheGoodies;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Reliable "run this in N server ticks" scheduler. {@code MinecraftServer.tell(new TickTask(...))}
 * does not reliably defer work when called from gameplay code (the server drains its task queue
 * within the same tick), so we count ticks ourselves off {@link ServerTickEvent.Post}.
 */
@EventBusSubscriber(modid = AllTheGoodies.MODID)
public final class ATGScheduler {

    private record Task(long runAtTick, Runnable action) {}

    private static final List<Task> TASKS = new ArrayList<>();

    /** Run {@code action} {@code delayTicks} server ticks from now. */
    public static void schedule(MinecraftServer server, int delayTicks, Runnable action) {
        TASKS.add(new Task(server.getTickCount() + Math.max(1, delayTicks), action));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (TASKS.isEmpty()) return;
        long now = event.getServer().getTickCount();

        // Collect & remove due tasks first, THEN run them — an action may schedule more tasks,
        // which would otherwise mutate TASKS mid-iteration (ConcurrentModificationException).
        List<Runnable> due = new ArrayList<>();
        Iterator<Task> it = TASKS.iterator();
        while (it.hasNext()) {
            Task t = it.next();
            if (now >= t.runAtTick()) {
                due.add(t.action());
                it.remove();
            }
        }
        for (Runnable action : due) {
            action.run();
        }
    }

    private ATGScheduler() {}
}

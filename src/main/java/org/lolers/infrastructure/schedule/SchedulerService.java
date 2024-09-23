package org.lolers.infrastructure.schedule;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SchedulerService {
    private static final Logger LOGGER = Logger.getLogger(SchedulerService.class.getName());

    private static final int POOL_SIZE = 15;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(POOL_SIZE);

    public static void scheduleTask(Runnable task, long delay, TimeUnit timeUnit) {
        LOGGER.info(String.format("scheduleTask:: Init scheduled task, delay: %d, timeUnit: %s", delay, timeUnit));
        scheduler.schedule(task, delay, timeUnit);
    }
}

package org.lolers.infrastructure.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerService.class.getName());

    private static final int POOL_SIZE = 15;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(POOL_SIZE);

    public static void scheduleTask(Runnable task, long delay, TimeUnit timeUnit) {
        LOGGER.info("scheduleTask:: Init scheduled task, delay: {}, timeUnit: {}", delay, timeUnit);
        scheduler.schedule(task, delay, timeUnit);
    }
}

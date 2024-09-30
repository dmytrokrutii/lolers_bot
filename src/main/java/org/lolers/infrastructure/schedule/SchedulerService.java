package org.lolers.infrastructure.schedule;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class SchedulerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerService.class.getName());
    private static final int POOL_SIZE = 15;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(POOL_SIZE);

    public void scheduleTask(Runnable task, long delay, TimeUnit timeUnit) {
        LOGGER.info("scheduleTask:: Init scheduled task, delay: {}, timeUnit: {}", delay, timeUnit);
        scheduler.schedule(task, delay, timeUnit);
    }
}

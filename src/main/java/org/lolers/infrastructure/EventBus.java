package org.lolers.infrastructure;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.engio.mbassy.bus.MBassador;
import org.lolers.event.UpdateReceivedEventHandler;

@Singleton
public class EventBus {
    private static MBassador<Object> bus;

    @Inject
    public EventBus(UpdateReceivedEventHandler updateEventHandler) {
        bus = new MBassador<>();
        bus.subscribe(updateEventHandler);
    }

    public void postEvent(Object event) {
        bus.post(event).asynchronously();
    }
}

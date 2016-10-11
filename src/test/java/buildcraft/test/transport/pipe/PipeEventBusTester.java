package buildcraft.test.transport.pipe;

import org.junit.Assert;
import org.junit.Test;

import buildcraft.api.transport.PipeEventHandler;
import buildcraft.api.transport.PipeEventItem;
import buildcraft.transport.pipe.PipeEventBus;

public class PipeEventBusTester {
    public static long dontInlineThis = 0;

    @Test
    public void testSimpleEvent() {
        PipeEventBus bus = new PipeEventBus();

        PipeEventItem.ModifySpeed event = new PipeEventItem.ModifySpeed(null, null, null, 1);
        bus.fireEvent(event);
        Assert.assertEquals(1, event.speed, 0.00001);

        bus.registerHandler(this);

        event = new PipeEventItem.ModifySpeed(null, null, null, 1);
        bus.fireEvent(event);
        Assert.assertEquals(2, event.speed, 0.00001);

        bus.unregisterHandler(this);

        event = new PipeEventItem.ModifySpeed(null, null, null, 1);
        bus.fireEvent(event);
        Assert.assertEquals(1, event.speed, 0.00001);
    }

    @Test
    public void testSpeed() {
        dontInlineThis = 0;
        PipeEventBus bus = new PipeEventBus();
        bus.registerHandler(this);
        for (int i = 0; i < 10_000_000; i++) {
            PipeEventItem.ModifySpeed event = new PipeEventItem.ModifySpeed(null, null, null, 2);
            bus.fireEvent(event);
            dontInlineThis += event.speed;
        }
        System.out.println(dontInlineThis);
    }

    @PipeEventHandler
    public void modifySpeed(PipeEventItem.ModifySpeed event) {
        event.speed++;
    }
}

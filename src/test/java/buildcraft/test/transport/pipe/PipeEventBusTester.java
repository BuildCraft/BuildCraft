package buildcraft.test.transport.pipe;

import org.junit.Assert;
import org.junit.Test;

import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;

import buildcraft.transport.pipe.PipeEventBus;

public class PipeEventBusTester {
    public static long dontInlineThis = 0;

    @Test
    public void testSimpleEvent() {
        PipeEventBus bus = new PipeEventBus();

        PipeEventItem.ModifySpeed event = new PipeEventItem.ModifySpeed(null, null, null, 1);
        bus.fireEvent(event);
        Assert.assertEquals(0, event.targetSpeed, 0.00001);

        bus.registerHandler(this);

        event = new PipeEventItem.ModifySpeed(null, null, null, 1);
        bus.fireEvent(event);
        Assert.assertEquals(1, event.targetSpeed, 0.00001);

        bus.unregisterHandler(this);

        event = new PipeEventItem.ModifySpeed(null, null, null, 1);
        bus.fireEvent(event);
        Assert.assertEquals(0, event.targetSpeed, 0.00001);
    }

    @PipeEventHandler
    public void modifySpeed(PipeEventItem.ModifySpeed event) {
        event.targetSpeed = 1;
    }
}

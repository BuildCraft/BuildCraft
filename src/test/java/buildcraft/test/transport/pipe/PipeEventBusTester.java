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

    @Test
    public void testExtends() {
        PipeEventBus bus = new PipeEventBus();

        PipeEventItem.ModifySpeed event = new PipeEventItem.ModifySpeed(null, null, null, 1);
        bus.fireEvent(event);
        Assert.assertEquals(0, event.targetSpeed, 0.00001);

        bus.registerHandler(new Base());

        event = new PipeEventItem.ModifySpeed(null, null, null, 1);
        bus.fireEvent(event);
        Assert.assertEquals(2, event.targetSpeed, 0.00001);

        bus = new PipeEventBus();
        bus.registerHandler(new Sub());

        event = new PipeEventItem.ModifySpeed(null, null, null, 1);
        bus.fireEvent(event);
        Assert.assertEquals(3, event.targetSpeed, 0.00001);
    }

    public static class Base {
        @PipeEventHandler
        public void modifySpeed2(PipeEventItem.ModifySpeed event) {
            event.targetSpeed = 2;
        }
    }

    public static class Sub extends Base {
        @Override
        public void modifySpeed2(PipeEventItem.ModifySpeed event) {
            event.targetSpeed = 3;
        }
    }
}

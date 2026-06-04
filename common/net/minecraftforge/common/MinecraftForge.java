// STUB(R.Chen): Forge MinecraftForge — compile shim. EVENT_BUS.register() is a no-op; use Fabric events instead.
package net.minecraftforge.common;

public class MinecraftForge {
    public static final EventBus EVENT_BUS = new EventBus();

    public static class EventBus {
        public void register(Object listener) {
            // no-op — use Fabric lifecycle callbacks / Events instead
        }
        public void unregister(Object listener) {
            // no-op
        }
    }
}

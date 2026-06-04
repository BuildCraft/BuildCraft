// STUB(R.Chen): Forge ConfigChangedEvent — compile shim.
package net.minecraftforge.fml.client.event;

public class ConfigChangedEvent {
    public static class OnConfigChangedEvent {
        private final String modID;
        public OnConfigChangedEvent(String modID) { this.modID = modID; }
        public String getModID() { return modID; }
    }
}

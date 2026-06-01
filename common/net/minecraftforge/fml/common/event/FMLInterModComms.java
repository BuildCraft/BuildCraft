// STUB(R.Chen): Forge FMLInterModComms — compile shim.
package net.minecraftforge.fml.common.event;

public class FMLInterModComms {
    public static class IMCEvent {}
    public static class IMCMessage {
        public String getSender() { return ""; }
        public String getKey() { return ""; }
        public boolean isStringMessage() { return false; }
        public String getStringValue() { return ""; }
    }
    public static void sendMessage(String modId, String key, String value) {}
}

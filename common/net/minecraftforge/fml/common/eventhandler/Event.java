// STUB(R.Chen): Forge Event — compile shim.
package net.minecraftforge.fml.common.eventhandler;

public class Event {
    public enum Result { DENY, DEFAULT, ALLOW }

    private boolean canceled;

    public boolean isCancelable() { return false; }
    public boolean isCanceled() { return canceled; }
    public void setCanceled(boolean cancel) { this.canceled = cancel; }
}

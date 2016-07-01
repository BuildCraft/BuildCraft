package buildcraft.api.mj;

/** A specialised receiver for redstone power. */
public interface IRedstoneReceiver extends IMjConductor {
    /** Receives power. You are encouraged to either:
     * <ul>
     * <li>Use up all power immediately
     * <li>Store all power in something like an {@link MjBattery} for later usage.
     * <li>Refuse all power (if you have no more work to do or your {@link MjBattery} is full).
     * </ul>
     * 
     * @param milliJoules The number of millijoules to add
     * @param simulate If true then just pretend you received power- don't actually change any of your internal state.
     * @return True if all the power was accepted, false if not. */
    boolean receiveRedstonePower(int milliJoules, boolean simulate);
}

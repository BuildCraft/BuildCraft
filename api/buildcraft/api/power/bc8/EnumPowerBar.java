package buildcraft.api.power.bc8;

/** Defines the 2 types of power bars: redstone and full. */
public enum EnumPowerBar {
    REDSTONE(1),
    FULL(4);

    /** The number of units this transfers. */
    public final int units;
    /** The number of RF this transfers per second. */
    public final int rfSecond;

    EnumPowerBar(int units) {
        this.units = units;
        this.rfSecond = units * 50;
    }

    /** @return True if this power bar supplies the given type. (Essentially if you want {@link #REDSTONE} then it may
     *         be supplied by either normal engines or redstone engines, but if you want {@link FULL} then it will only
     *         be supplied by normal engines. */
    public boolean supplies(EnumPowerBar bar) {
        return bar == this || bar == FULL;
    }
}

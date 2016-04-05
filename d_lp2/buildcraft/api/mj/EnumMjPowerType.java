package buildcraft.api.mj;

public enum EnumMjPowerType {
    /** Low power that is used to power only very simple automata (extraction from chests for example). Usually only
     * 10-100 milli watts of power. Generally can only be transfered over tiny distances- a single block is pushing
     * it. */
    REDSTONE,
    /** Medium power that is used for most machines that require big complex tasks being done (filling, quarrying,
     * building.) Usually 1-60 watts of power. Generally can be transported over medium distances (a sane distance
     * is up to 100 blocks) */
    THINK_OF_NAME,
    /** High power used for expensive, but delicate tasks (creating an iron chipset or programming a robot board).
     * Generally can only be transfered over short distance. */
    LASER
}

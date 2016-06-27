package buildcraft.api.mj;

/** Designates a machine that provides power passively- it does not attempt to manually output its power (like an
 * engine). Power can be extracted from this by powered wooden kinesis pipes, for example. */
public interface IMjPassiveProvider extends IMjConductor {
    /** Attempts to extract power from this provider
     * 
     * @param simulate
     * @return Either 0, min, max, or a value between min and max. */
    int extractPower(int min, int max, boolean simulate);
}

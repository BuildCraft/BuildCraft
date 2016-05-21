package buildcraft.api.mj;

/** Signifies that this should visibly connect to kinesis pipes. This should NEVER be the tile entity, but an
 * encapsulated class that refers back to it. */
public interface IMjConductor {
    boolean canConnect(IMjConductor other);
}

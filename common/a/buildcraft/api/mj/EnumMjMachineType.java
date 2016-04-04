package a.buildcraft.api.mj;

/** This isn't required. */
@Deprecated
public enum EnumMjMachineType {
    /** Can produce a form of power from some external source, say from burning coal or oil. It could even be from
     * sunlight. */
    PRODUCER,
    /** Something that moves power from one place to another */
    CONDUCTOR,
    /** Laser block, power adaptor */
    CONVERTER,
    /** quarry, filler, etc */
    CONSUMER;
}

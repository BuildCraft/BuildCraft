package ct.buildcraft.api;

/** Allows a mod (or addon) to register networking packets in the message manager. */
public interface IBuildCraftMod {
    /** @return The modid to use when registering this as a channel. */
    String getModId();
}

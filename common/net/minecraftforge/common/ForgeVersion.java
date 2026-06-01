// STUB(R.Chen): Forge ForgeVersion — compile shim.
package net.minecraftforge.common;

public class ForgeVersion {
    public enum Status { BETA, BETA_OUTDATED, CURRENT, OUTDATED, AHEAD, PENDING, FAILED, UP_TO_DATE, UNOFFICIAL }

    public static String getVersion() { return "STUB"; }
    public static Status getStatus() { return Status.UP_TO_DATE; }
}

package buildcraft.api.mj;

import java.text.DecimalFormat;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import buildcraft.api.APIHelper;

public class MjAPI {

    // ################################
    //
    // Useful constants (Public API)
    //
    // ################################

    /** A single minecraft joule, in micro joules (the power system base unit) */
    public static final long ONE_MINECRAFT_JOULE = 1_000_000L;
    /** The same as {@link #ONE_MINECRAFT_JOULE}, but a shorter field name */
    public static final long MJ = ONE_MINECRAFT_JOULE;

    /** The decimal format used to display values of MJ to the player. Note that this */
    public static final DecimalFormat MJ_DISPLAY_FORMAT = new DecimalFormat("###0.##");

    public static final IMjEffectManager EFFECT_MANAGER = APIHelper.getInstance("", IMjEffectManager.class, VoidEffectManager.INSTANCE);

    // ###############
    //
    // Capabilities
    //
    // ###############

    @CapabilityInject(IMjConnector.class)
    public static final Capability<IMjConnector> CAP_CONNECTOR = null;

    @CapabilityInject(IMjReceiver.class)
    public static final Capability<IMjReceiver> CAP_RECEIVER = null;

    @CapabilityInject(IMjReadable.class)
    public static final Capability<IMjReadable> CAP_READABLE = null;

    @CapabilityInject(IMjPassiveProvider.class)
    public static final Capability<IMjPassiveProvider> CAP_PASSIVE_PROVIDER = null;

    // ###############
    //
    // Helpful methods
    //
    // ###############

    /** Formats a given MJ value to a player-oriented string. Note that this does not append "Mj" to the value. */
    public static String formatMj(long microMj) {
        return formatMjInternal(microMj / (double) MJ);
    }

    /** Formats a given MJ value to a player-oriented string. Note that this DOES append "*Mj" to the value. This does
     * however shorten it down to a small length, and displays "µ", "m", "k" or "g" before the MJ depending on how big
     * or small the value is. */
    public static String formatMjShort(long microJoules) {
        if (microJoules == 0) {
            return "0 Mj";
        }
        long limit = 1;
        final long nextUnitCap = 800;
        if (microJoules < nextUnitCap * limit) {// micro MJ
            return formatMjInternal(microJoules) + " µMj";
        }
        limit *= 1000;
        if (microJoules < nextUnitCap * limit) { // milli MJ
            return formatMjInternal(microJoules / (double) limit) + " mMj";
        }
        limit *= 1000;
        if (microJoules < nextUnitCap * limit) { // MJ
            return formatMjInternal(microJoules / (double) limit) + " Mj";
        }
        limit *= 1000;
        if (microJoules < nextUnitCap * limit) {// kilo MJ
            return formatMjInternal(microJoules / (double) limit) + " KMj";
        }
        limit *= 1000;
        if (microJoules < nextUnitCap * limit) {// mega MJ
            return formatMjInternal(microJoules / (double) limit) + " MMj";
        }
        limit *= 1000;
        return formatMjInternal(microJoules / (double) limit) + " GMj";
    }

    // ########################################
    //
    // Null based classes of the capabilities
    //
    // ########################################

    // @formatter:off
    public enum VoidConductor implements IMjConnector {
        INSTANCE;
        @Override public boolean canConnect(IMjConnector other) {return false;}
        @Override public IMjConnectorType getType() {return MjSimpleType.KINETIC_TRANSPORTER;}
    }

    public enum VoidRequestor implements IMjReceiver {
        INSTANCE;
        @Override public boolean canConnect(IMjConnector other) {return false;}
        @Override public long getPowerRequested() {return 0;}
        @Override public boolean receivePower(long microJoules, boolean simulate) {return false;}
        @Override public IMjConnectorType getType() {return MjSimpleType.KINETIC_CONSUMER;}
    }

    public enum VoidPassiveProvider implements IMjPassiveProvider {
        INSTANCE;
        @Override public boolean canConnect(IMjConnector other) {return false;}
        @Override public long extractPower(long min, long max, boolean simulate) {return 0;}
        @Override public IMjConnectorType getType() {return MjSimpleType.KINETIC_PRODUCER;}
    }

    public enum VoidEffectManager implements IMjEffectManager {
        INSTANCE;
        @Override public void createPowerLossEffect(World world, Vec3d center, long microJoulesLost) {}
        @Override public void createPowerLossEffect(World world, Vec3d center, EnumFacing direction, long microJoulesLost) {}
        @Override public void createPowerLossEffect(World world, Vec3d center, Vec3d direction, long microJoulesLost) {}
    }
    // @formatter:on

    // ####################
    //
    // Internal API logic
    //
    // ###################

    static {
        registerCapability(IMjReceiver.class);
        registerCapability(IMjReadable.class);
        registerCapability(IMjConnector.class);
        registerCapability(IMjPassiveProvider.class);

        ensureRegistration(CAP_CONNECTOR, "connector");
        ensureRegistration(CAP_RECEIVER, "receiver");
        ensureRegistration(CAP_READABLE, "readable");
        ensureRegistration(CAP_PASSIVE_PROVIDER, "passive provider");
    }

    private static <T> void registerCapability(Class<T> clazz) {
        CapabilityManager.INSTANCE.register(clazz, new VoidStorage<T>(), () -> {
            throw new IllegalStateException("You must create your own instances!");
        });
    }

    private static void ensureRegistration(Capability<?> capReceiver, String name) {
        if (capReceiver == null) {
            throw new Error("Capability registration failed for " + name);
        }
    }

    private static class VoidStorage<T> implements Capability.IStorage<T> {
        @Override
        public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
            throw new IllegalStateException("You must create your own instances!");
        }

        @Override
        public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
            throw new IllegalStateException("You must create your own instances!");
        }
    }

    private static String formatMjInternal(double val) {
        return MJ_DISPLAY_FORMAT.format(val);
    }
}

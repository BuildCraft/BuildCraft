package ct.buildcraft.api.mj;

import java.text.DecimalFormat;

import javax.annotation.Nonnull;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class MjAPI {

    // ################################
    //
    // Useful constants (Public API)
    //
    // ################################

    /** A single minecraft joule, in micro joules (the power system base unit) */
    public static final long ONE_MINECRAFT_JOULE = getMjValue();
    /** The same as {@link #ONE_MINECRAFT_JOULE}, but a shorter field name */
    public static final long MJ = ONE_MINECRAFT_JOULE;

    /** The decimal format used to display values of MJ to the player. Note that this */
    public static final DecimalFormat MJ_DISPLAY_FORMAT = new DecimalFormat("#,##0.##");

    public static IMjEffectManager EFFECT_MANAGER = NullaryEffectManager.INSTANCE;

    // ###############
    //
    // Helpful methods
    //
    // ###############

    /** Formats a given MJ value to a player-oriented string. Note that this does not append "MJ" to the value. */
    public static String formatMj(long microMj) {
        return formatMjInternal(microMj / (double) MJ);
    }

    private static String formatMjInternal(double val) {
        return MJ_DISPLAY_FORMAT.format(val);
    }

    // ########################################
    //
    // Null based classes
    //
    // ########################################

    public enum NullaryEffectManager implements IMjEffectManager {
        INSTANCE;
        @Override
        public void createPowerLossEffect(Level world, Vec3 center, long microJoulesLost) {}

        @Override
        public void createPowerLossEffect(Level world, Vec3 center, Direction direction, long microJoulesLost) {}

        @Override
        public void createPowerLossEffect(Level world, Vec3 center, Vec3 direction, long microJoulesLost) {}
    }
    // @formatter:on

    // ###############
    //
    // Capabilities
    //
    // ###############

    @Nonnull
    public static final Capability<IMjConnector> CAP_CONNECTOR;

    @Nonnull
    public static final Capability<IMjReceiver> CAP_RECEIVER;

    @Nonnull
    public static final Capability<IMjRedstoneReceiver> CAP_REDSTONE_RECEIVER;

    @Nonnull
    public static final Capability<IMjReadable> CAP_READABLE;

    @Nonnull
    public static final Capability<IMjPassiveProvider> CAP_PASSIVE_PROVIDER;

    static {
        CAP_CONNECTOR = CapabilityManager.get(new CapabilityToken<>(){});
        CAP_RECEIVER = CapabilityManager.get(new CapabilityToken<>(){});
        CAP_REDSTONE_RECEIVER = CapabilityManager.get(new CapabilityToken<>(){});
        CAP_READABLE = CapabilityManager.get(new CapabilityToken<>(){});
        CAP_PASSIVE_PROVIDER = CapabilityManager.get(new CapabilityToken<>(){});
    }

    private static long getMjValue() {
        return 1_000_000L;
    }
}

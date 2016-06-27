package buildcraft.api.mj;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import buildcraft.api.APIHelper;

public class MjAPI {
    @CapabilityInject(IMjConductor.class)
    public static final Capability<IMjConductor> CAP_CONDUCTOR = null;
    public static final IMjEffectManager EFFECT_MANAGER = APIHelper.getInstance("", IMjEffectManager.class, VoidEffectManager.INSTANCE);

    static {
        CapabilityManager.INSTANCE.register(IMjConductor.class, VoidStorage.INSTANCE, () -> {
            throw new IllegalStateException("You must create your own instances!");
        });
    }

    private enum VoidStorage implements Capability.IStorage<IMjConductor> {
        INSTANCE;

        @Override
        public NBTBase writeNBT(Capability<IMjConductor> capability, IMjConductor instance, EnumFacing side) {
            throw new IllegalStateException("You must create your own instances!");
        }

        @Override
        public void readNBT(Capability<IMjConductor> capability, IMjConductor instance, EnumFacing side, NBTBase nbt) {
            throw new IllegalStateException("You must create your own instances!");
        }
    }

    // @formatter:off
    public enum VoidConductor implements IMjConductor {
        INSTANCE;
        @Override public boolean canConnect(IMjConductor other) {return false;}
    }

    public enum VoidRequestor implements IMjReciever {
        INSTANCE;
        @Override public boolean canConnect(IMjConductor other) {return false;}
        @Override public int getPowerRequested() {return 0;}
        @Override public boolean receivePower(int milliJoules, boolean simulate) {return false;}
    }

    public enum VoidPassiveProvider implements IMjPassiveProvider {
        INSTANCE;
        @Override public boolean canConnect(IMjConductor other) {return false;}
        @Override public int extractPower(int min, int max, boolean simulate) {return 0;}
    }

    public enum VoidEffectManager implements IMjEffectManager {
        INSTANCE;
        @Override public void createPowerLossEffect(World world, Vec3d center, int joulesLost) {}
        @Override public void createPowerLossEffect(World world, Vec3d center, EnumFacing direction, int joulesLost) {}
        @Override public void createPowerLossEffect(World world, Vec3d center, Vec3d direction, int joulesLost) {}
    }
    // @formatter:on
}

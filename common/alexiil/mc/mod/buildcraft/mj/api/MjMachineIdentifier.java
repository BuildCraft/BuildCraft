package alexiil.mc.mod.buildcraft.mj.api;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

/** Used to uniquely identify a machine in the world from {@link ICapabilityProvider}
 * 
 * @date Created on 2 Apr 2016 by AlexIIL */
public final class MjMachineIdentifier {
    /** The dimension of the world that this machine is in. */
    public final int dimension;
    /** The position inside the world that this machine is currently contained at. */
    @Nonnull
    public final BlockPos pos;
    /** The face that this machine can be accessed from. Note that null *is* allowed as you can pass null to
     * {@link ICapabilityProvider#getCapability(net.minecraftforge.common.capabilities.Capability, EnumFacing)} */
    @Nullable
    public final EnumFacing face;
    private final int hash;

    public MjMachineIdentifier(int dimension, BlockPos pos, EnumFacing face) {
        if (pos == null) throw new NullPointerException("pos");
        this.dimension = dimension;
        this.pos = pos;
        this.face = face;
        hash = Objects.hash(dimension, pos, face);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MjMachineIdentifier other = (MjMachineIdentifier) obj;
        if (dimension != other.dimension) return false;
        if (face != other.face) return false;
        if (!pos.equals(other.pos)) return false;
        return true;
    }
}

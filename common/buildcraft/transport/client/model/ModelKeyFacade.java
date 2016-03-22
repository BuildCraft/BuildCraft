package buildcraft.transport.client.model;

import java.util.Arrays;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public class ModelKeyFacade extends PluggableModelKey<ModelKeyFacade> {
    public final boolean hollow;
    public final IBlockState state;
    private final int hash;

    public ModelKeyFacade(EnumFacing side, IBlockState state, boolean hollow) {
        super(EnumWorldBlockLayer.CUTOUT, FacadePluggableModel.INSTANCE, side);
        this.hollow = hollow;
        this.state = state;
        hash = Arrays.hashCode(new int[] { super.hashCode(), state.hashCode(), Boolean.hashCode(hollow) });
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        ModelKeyFacade other = (ModelKeyFacade) obj;
        if (!state.equals(other.state)) return false;
        return true;
    }
}

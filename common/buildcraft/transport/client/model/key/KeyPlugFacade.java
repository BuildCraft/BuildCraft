package buildcraft.transport.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import java.util.Objects;

public class KeyPlugFacade extends PluggableModelKey {
    public final IBlockState state;
    public final boolean isHollow;
    private final int hash;

    public KeyPlugFacade(BlockRenderLayer layer, EnumFacing side, IBlockState state, boolean isHollow) {
        super(layer, side);
        this.state = state;
        this.isHollow = isHollow;
        this.hash = Objects.hash(layer, side, state, isHollow);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        KeyPlugFacade other = (KeyPlugFacade) obj;
        return other.isHollow == isHollow//
                && other.layer == layer//
                && other.state == state//
                && other.side == side;
    }
}

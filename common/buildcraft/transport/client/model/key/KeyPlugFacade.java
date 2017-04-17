package buildcraft.transport.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import java.util.Objects;

public class KeyPlugFacade extends PluggableModelKey {
    public KeyPlugFacade(BlockRenderLayer layer, EnumFacing side) {
        super(layer, side);
    }

    @Override
    public int hashCode() {
        return Objects.hash(layer, side);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        KeyPlugFacade other = (KeyPlugFacade) obj;
        return other.layer == layer && other.side == side;
    }
}

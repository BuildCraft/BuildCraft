package buildcraft.transport.client.model.key;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public final class KeyPlugBlocker extends PluggableModelKey {
    public KeyPlugBlocker(EnumFacing side) {
        super(BlockRenderLayer.CUTOUT, side);
    }
}

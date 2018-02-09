package buildcraft.transport.client.model.key;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public class KeyPlugPowerAdaptor extends PluggableModelKey {
    public KeyPlugPowerAdaptor(EnumFacing side) {
        super(BlockRenderLayer.CUTOUT, side);
    }
}

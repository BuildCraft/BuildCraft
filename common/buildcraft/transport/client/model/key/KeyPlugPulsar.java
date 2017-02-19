package buildcraft.transport.client.model.key;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public class KeyPlugPulsar extends PluggableModelKey {
    public KeyPlugPulsar(EnumFacing side) {
        super(BlockRenderLayer.CUTOUT, side);
    }
}

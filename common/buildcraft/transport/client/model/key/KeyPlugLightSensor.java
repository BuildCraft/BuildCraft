package buildcraft.transport.client.model.key;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public class KeyPlugLightSensor extends PluggableModelKey {
    public KeyPlugLightSensor(EnumFacing side) {
        super(BlockRenderLayer.CUTOUT, side);
    }
}

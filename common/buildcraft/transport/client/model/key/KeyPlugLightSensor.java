package buildcraft.transport.client.model.key;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.transport.client.model.plug.PlugBakerLightSensor;

public class KeyPlugLightSensor extends PluggableModelKey<KeyPlugLightSensor> {
    public KeyPlugLightSensor(EnumFacing side) {
        super(BlockRenderLayer.CUTOUT, PlugBakerLightSensor.INSTANCE, side);
    }
}

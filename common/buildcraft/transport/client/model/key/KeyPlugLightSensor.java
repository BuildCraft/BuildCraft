package buildcraft.transport.client.model.key;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.transport.BCTransportModels;

public class KeyPlugLightSensor extends PluggableModelKey<KeyPlugLightSensor> {
    public KeyPlugLightSensor(EnumFacing side) {
        super(BlockRenderLayer.CUTOUT, BCTransportModels.BAKER_PLUG_LIGHT_SENSOR, side);
    }
}

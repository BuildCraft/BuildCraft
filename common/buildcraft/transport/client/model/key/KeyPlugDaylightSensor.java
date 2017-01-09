package buildcraft.transport.client.model.key;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.transport.client.model.plug.PlugBakerDaylightSensor;

public class KeyPlugDaylightSensor extends PluggableModelKey<KeyPlugDaylightSensor> {
    public KeyPlugDaylightSensor(EnumFacing side) {
        super(BlockRenderLayer.CUTOUT, PlugBakerDaylightSensor.INSTANCE, side);
        }
}

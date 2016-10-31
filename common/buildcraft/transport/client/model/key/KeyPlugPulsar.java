package buildcraft.transport.client.model.key;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.transport.client.model.plug.PlugBakerPulsar;

public class KeyPlugPulsar extends PluggableModelKey<KeyPlugPulsar> {
    public KeyPlugPulsar(EnumFacing side) {
        super(BlockRenderLayer.CUTOUT, PlugBakerPulsar.INSTANCE, side);
    }
}

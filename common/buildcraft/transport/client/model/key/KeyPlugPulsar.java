package buildcraft.transport.client.model.key;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.transport.BCTransportModels;

public class KeyPlugPulsar extends PluggableModelKey<KeyPlugPulsar> {
    public KeyPlugPulsar(EnumFacing side) {
        super(BlockRenderLayer.CUTOUT, BCTransportModels.BAKER_PLUG_PULSAR, side);
    }
}

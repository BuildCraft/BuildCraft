package buildcraft.transport.client.model.key;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.transport.client.model.plug.PlugBakerStopper;

public final class KeyPlugStopper extends PluggableModelKey<KeyPlugStopper> {
    public KeyPlugStopper(EnumFacing side) {
        super(BlockRenderLayer.CUTOUT, PlugBakerStopper.INSTANCE, side);
    }
}

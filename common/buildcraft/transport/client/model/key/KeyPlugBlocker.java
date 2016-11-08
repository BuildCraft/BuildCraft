package buildcraft.transport.client.model.key;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.transport.client.model.plug.PlugBakerBlocker;

public final class KeyPlugBlocker extends PluggableModelKey<KeyPlugBlocker> {
    public KeyPlugBlocker(EnumFacing side) {
        super(BlockRenderLayer.CUTOUT, PlugBakerBlocker.INSTANCE, side);
    }
}

package buildcraft.transport.plug;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.misc.RotationUtil;
import buildcraft.transport.client.model.key.KeyPlugFacade;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

public class PluggableFacade extends PipePluggable {
    public PluggableFacade(PluggableDefinition definition, IPipeHolder holder, EnumFacing side) {
        super(definition, holder, side);
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return RotationUtil.rotateAABB(new AxisAlignedBB(0 / 16D, 0 / 16D, 0 / 16D, 16 / 16D, 1 / 16D, 16 / 16D), side);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public PluggableModelKey getModelRenderKey(BlockRenderLayer layer) {
        switch (layer) {
            case CUTOUT:
            case TRANSLUCENT:
                return new KeyPlugFacade(layer, side);
            default:
                return null;
        }
    }
}

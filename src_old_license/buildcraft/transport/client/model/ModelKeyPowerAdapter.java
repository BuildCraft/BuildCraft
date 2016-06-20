package buildcraft.transport.client.model;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public class ModelKeyPowerAdapter extends PluggableModelKey<ModelKeyPowerAdapter> {
    public ModelKeyPowerAdapter(EnumFacing face) {
        super(BlockRenderLayer.CUTOUT, ModelPowerAdapter.INSTANCE, face);
    }

    // We don't need to redeclare hashCode and equals as the super class does it for us.
}

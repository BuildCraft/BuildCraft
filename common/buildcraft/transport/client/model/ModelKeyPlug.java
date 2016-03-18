package buildcraft.transport.client.model;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public class ModelKeyPlug extends PluggableModelKey<ModelKeyPlug> {
    public ModelKeyPlug(EnumFacing face) {
        super(EnumWorldBlockLayer.CUTOUT, PlugPluggableModel.INSTANCE, face);
    }

    // We don't need to redeclare hashCode and equals as the super class does it for us.
}

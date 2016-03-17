package buildcraft.transport.client.model;

import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pluggable.PluggableModelKeyCutout;

public class ModelKeyPlug extends PluggableModelKeyCutout<ModelKeyPlug> {
    public ModelKeyPlug(EnumFacing face) {
        super(PlugPluggableModel.INSTANCE, face);
    }

    // We don't need to redeclare hashCode and equals as the super class does it for us.
}

package ct.buildcraft.transport.client.model.key;

import ct.buildcraft.api.transport.pluggable.PluggableModelKey;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;

public class KeyPlugPowerAdaptor extends PluggableModelKey {
    public KeyPlugPowerAdaptor(Direction side) {
        super(RenderType.cutout(), side);
    }
}

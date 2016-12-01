package buildcraft.lib.inventory;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

import buildcraft.api.transport.IInjectable;

public class ItemInjectableHelper {

    public static IInjectable getIjectable(ICapabilityProvider provider, EnumFacing face) {
        if (provider == null) {
            return null;
        }
        if (provider instanceof IInjectable) {
            return (IInjectable) provider;
        }
        // TODO: get a capability from the provider!
        return null;
    }
}

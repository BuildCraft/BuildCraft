package ct.buildcraft.api.mj;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
/** Indicates an MJ consumer that has readable information. */
public interface IMjReadable extends IMjConnector {
    long getStored();

    long getCapacity();
}

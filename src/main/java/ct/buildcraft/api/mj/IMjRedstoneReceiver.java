package ct.buildcraft.api.mj;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
/** Designates that a receiver can receive redstone power (cheap, free, small amounts) */
public interface IMjRedstoneReceiver extends IMjReceiver {}

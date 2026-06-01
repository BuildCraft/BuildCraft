// STUB(R.Chen): Forge ICapabilityProvider — compile shim.
package net.minecraftforge.common.capabilities;

import javax.annotation.Nullable;

import net.minecraft.util.math.Direction;

public interface ICapabilityProvider {
    @Nullable
    <T> T getCapability(Capability<T> capability, @Nullable Direction facing);
}

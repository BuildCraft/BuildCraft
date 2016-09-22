package buildcraft.transport.api_move;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;

public abstract class PipePluggable {
    public final IPipeHolder holder;
    public final EnumFacing side;

    public PipePluggable(IPipeHolder holder, EnumFacing side) {
        this.holder = holder;
        this.side = side;
    }

    public void onTick() {}

    public boolean isBlocking() {
        return false;
    }

    public boolean hasCapability(Capability<?> cap) {
        return false;
    }

    public <T> T getCapability(Capability<T> cap) {
        return null;
    }
}

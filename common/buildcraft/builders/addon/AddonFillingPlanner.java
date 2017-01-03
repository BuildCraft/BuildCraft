package buildcraft.builders.addon;

import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.AddonDefaultRenderer;
import buildcraft.core.marker.volume.IFastAddonRenderer;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class AddonFillingPlanner extends Addon {
    @Override
    public IFastAddonRenderer getRenderer() {
        return new AddonDefaultRenderer();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }
}

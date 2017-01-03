package buildcraft.builders.addon;

import buildcraft.builders.BCBuildersGuis;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.AddonDefaultRenderer;
import buildcraft.core.marker.volume.IFastAddonRenderer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class AddonFillingPlanner extends Addon {
    @Override
    public IFastAddonRenderer getRenderer() {
        return new AddonDefaultRenderer();
    }

    @Override
    public void onPlayerRightClick(EntityPlayer player) {
        BCBuildersGuis.FILLING_PLANNER.openGUI(player);
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

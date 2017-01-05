package buildcraft.core.marker.volume;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class Addon {
    public VolumeBox box;

    @SideOnly(Side.CLIENT)
    public abstract IFastAddonRenderer getRenderer();

    public EnumAddonSlot getSlot() {
        return box.addons.entrySet().stream().filter(slotAddon -> slotAddon.getValue() == this).findFirst().orElse(null).getKey();
    }

    public AxisAlignedBB getBoundingBox() {
        return getSlot().getBoundingBox(box);
    }

    public void onAdded() {
    }

    public void onRemoved() {
    }

    public void onPlayerRightClick(EntityPlayer player) {
    }

    public abstract NBTTagCompound writeToNBT(NBTTagCompound nbt);

    public abstract void readFromNBT(NBTTagCompound nbt);

    public abstract void toBytes(ByteBuf buf);

    public abstract void fromBytes(ByteBuf buf);
}

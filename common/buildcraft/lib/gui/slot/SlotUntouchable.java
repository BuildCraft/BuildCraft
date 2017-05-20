/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.tile.item.IItemHandlerAdv;

import javax.annotation.Nonnull;

public class SlotUntouchable extends SlotBase implements IPhantomSlot {

    public SlotUntouchable(IItemHandlerAdv itemHandler, int slotIndex, int posX, int posY) {
        super(itemHandler, slotIndex, posX, posY);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack itemstack) {
        return false;
    }

    @Override
    public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
        return false;
    }

    @Override
    public boolean canAdjustCount() {
        return false;
    }

    @Override
    public boolean canShift() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canBeHovered() {
        return false;
    }
}

/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.gui.slot;

import net.minecraft.item.ItemStack;

import buildcraft.lib.tile.item.IItemHandlerAdv;

import javax.annotation.Nonnull;

public class SlotOutput extends SlotBase {

    public SlotOutput(IItemHandlerAdv handler, int slotIndex, int posX, int posY) {
        super(handler, slotIndex, posX, posY);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack itemstack) {
        return false;
    }
}

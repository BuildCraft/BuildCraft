/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.tile.item.StackInsertionFunction;
import net.minecraft.item.ItemStack;

public class TileFilteredBuffer extends TileBC_Neptune {
    public final ItemHandlerSimple invFilter;
    public final ItemHandlerSimple invMain;

    public TileFilteredBuffer() {
        invFilter = itemManager.addInvHandler("filter", 9, EnumAccess.PHANTOM);
        ItemHandlerSimple handler = new ItemHandlerSimple(9, this::canInsert, StackInsertionFunction.getDefaultInserter(), this::onSlotChange);
        invMain = itemManager.addInvHandler("main", handler, EnumAccess.BOTH, EnumPipePart.VALUES);
    }

    private boolean canInsert(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        ItemStack filterStack = invFilter.getStackInSlot(slot);
        return StackUtil.canMerge(filterStack, stack);
    }
}

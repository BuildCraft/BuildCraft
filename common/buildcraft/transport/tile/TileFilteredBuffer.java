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

public class TileFilteredBuffer extends TileBC_Neptune {
    public final ItemHandlerSimple invFilter = itemManager.addInvHandler(
        "filter",
        9,
        StackInsertionFunction.getInsertionFunction(1),
        EnumAccess.PHANTOM
    );
    public final ItemHandlerSimple invMain = itemManager.addInvHandler(
        "main",
        9,
        (slot, stack) -> stack.isEmpty() ||
            !invFilter.getStackInSlot(slot).isEmpty() && StackUtil.canMerge(invFilter.getStackInSlot(slot), stack),
        EnumAccess.BOTH,
        EnumPipePart.VALUES
    );
}

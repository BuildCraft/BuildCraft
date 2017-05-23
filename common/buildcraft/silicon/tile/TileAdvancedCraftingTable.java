/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import java.util.List;

import net.minecraft.util.EnumFacing;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;

public class TileAdvancedCraftingTable extends TileLaserTableBase {
    public final ItemHandlerSimple invBlueprint = itemManager.addInvHandler("blueprint", 3 * 3, ItemHandlerManager.EnumAccess.NONE);
    public final ItemHandlerSimple invMaterials = itemManager.addInvHandler("materials", 5 * 3, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
    public final ItemHandlerSimple invResults = itemManager.addInvHandler("result", 3 * 3, ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);

    public long getTarget() {
        return 40 * MjAPI.MJ;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);
        left.add("target - " + LocaleUtil.localizeMj(getTarget()));
    }

    @Override
    public void update() {
        super.update();

        if (world.isRemote) {
            return;
        }

        sendNetworkGuiUpdate(NET_GUI_DATA);
    }

    @Override
    public boolean hasWork() {
        return true;
    }
}

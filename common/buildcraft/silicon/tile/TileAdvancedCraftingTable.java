/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.factory.util.IAutoCraft;
import buildcraft.factory.util.WorkbenchCrafting;

public class TileAdvancedCraftingTable extends TileLaserTableBase implements IAutoCraft {
    private static final long POWER_REQ = 500 * MjAPI.MJ;

    public final ItemHandlerSimple invBlueprint = itemManager.addInvHandler(
        "blueprint",
        3 * 3,
        ItemHandlerManager.EnumAccess.PHANTOM
    );
    public final ItemHandlerSimple invMaterials = itemManager.addInvHandler(
        "materials",
        5 * 3,
        ItemHandlerManager.EnumAccess.INSERT,
        EnumPipePart.VALUES
    );
    public final ItemHandlerSimple invResults = itemManager.addInvHandler(
        "result",
        3 * 3,
        ItemHandlerManager.EnumAccess.EXTRACT,
        EnumPipePart.VALUES
    );
    private final WorkbenchCrafting crafting = new WorkbenchCrafting(3, 3, invBlueprint);
    public IRecipe currentRecipe;
    private List<ItemStack> requirements = null;

    @Override
    public long getTarget() {
        return canWork() ? POWER_REQ : 0;
    }

    @Override
    public void update() {
        super.update();
        updateRecipe();
        if (world.isRemote) {
            return;
        }
        if (power >= POWER_REQ) {
            power -= POWER_REQ;
            craft();
        }
        sendNetworkGuiUpdate(NET_GUI_DATA);
    }

    @Override
    public ItemHandlerSimple getInvBlueprint() {
        return invBlueprint;
    }

    @Override
    public ItemHandlerSimple getInvMaterials() {
        return invMaterials;
    }

    @Override
    public ItemHandlerSimple getInvResult() {
        return invResults;
    }

    @Override
    public WorkbenchCrafting getWorkbenchCrafting() {
        return crafting;
    }

    @Override
    public IRecipe getCurrentRecipe() {
        return currentRecipe;
    }

    @Override
    public void setCurrentRecipe(IRecipe recipe) {
        currentRecipe = recipe;
    }

    @Override
    public World getWorldForAutoCrafting() {
        return getWorld();
    }

    @Override
    public void setRequirements(List<ItemStack> stacks) {
        requirements = stacks;
    }

    @Override
    public List<ItemStack> getRequirements() {
        return requirements;
    }

    @Override
    public BlockPos getPosForAutoCrafting() {
        return getPos();
    }
}

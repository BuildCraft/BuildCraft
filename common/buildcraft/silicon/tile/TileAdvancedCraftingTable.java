/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.craft.IAutoCraft;
import buildcraft.lib.tile.craft.WorkbenchCrafting;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.container.ContainerAdvancedCraftingTable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class TileAdvancedCraftingTable extends TileLaserTableBase implements IAutoCraft {
    private static final long POWER_REQ = 500 * MjAPI.MJ;

    public final ItemHandlerSimple invBlueprint;
    public final ItemHandlerSimple invMaterials;
    public final ItemHandlerSimple invResults;
    private final WorkbenchCrafting crafting;

    public ItemStack resultClient = ItemStack.EMPTY;

    public TileAdvancedCraftingTable(BlockPos pos, BlockState blockState) {
        super(BCSiliconBlocks.advancedCraftingTableTile.get(), pos, blockState);
        invBlueprint = itemManager.addInvHandler("blueprint", 3 * 3, EnumAccess.PHANTOM);
        invMaterials = itemManager.addInvHandler("materials", 5 * 3, EnumAccess.INSERT, EnumPipePart.VALUES);
        invResults = itemManager.addInvHandler("result", 3 * 3, EnumAccess.EXTRACT, EnumPipePart.VALUES);
        crafting = new WorkbenchCrafting(3, 3, this, invBlueprint, invMaterials, invResults);
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before, @Nonnull ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (!ItemStack.matches(before, after)) {
            crafting.onInventoryChange(handler);
        }
    }

    @Override
    public long getTarget() {
        return level.isClientSide ? POWER_REQ : crafting.canCraft() ? POWER_REQ : 0;
    }

    @Override
    public void update() {
        super.update();
        if (level.isClientSide) {
            return;
        }
        boolean didChange = crafting.tick();
        if (crafting.canCraft()) {
            if (power >= POWER_REQ) {
                if (crafting.craft()) {
                    // This is used for #hasWork(), to ensure that it doesn't return
                    // false for the one tick in between crafts.
                    power -= POWER_REQ;
                }
            }
        }
        if (didChange) {
            sendNetworkGuiUpdate(NET_GUI_DATA);
        }
    }

    @Override
//    public void readPayload(int id, PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_GUI_DATA) {
                resultClient = buffer.readItem();
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_GUI_DATA) {
                buffer.writeItemStack(crafting.getAssumedResult(), false);
            }
        }
    }

    public CraftingContainer getWorkbenchCrafting() {
        return crafting;
    }

    // IAutoCraft

    @Override
    public ItemStack getCurrentRecipeOutput() {
        return crafting.getAssumedResult();
    }

    @Override
    public ItemHandlerSimple getInvBlueprint() {
        return invBlueprint;
    }

    // Calen added from MenuProvider
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ContainerAdvancedCraftingTable(BCSiliconMenuTypes.ADVANCED_CRAFTING_TABLE, id, player, this);
    }
}

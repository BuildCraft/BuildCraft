/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.tile;

import java.io.IOException;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.lib.tile.craft.IAutoCraft;
import ct.buildcraft.lib.tile.craft.WorkbenchCrafting;
import ct.buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.silicon.BCSiliconBlocks;
import ct.buildcraft.silicon.container.ContainerAdvancedCraftingTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class TileAdvancedCraftingTable extends TileLaserTableBase implements IAutoCraft, MenuProvider {
    private static final long POWER_REQ = 500 * MjAPI.MJ;

    public final ItemHandlerSimple invBlueprint;
    public final ItemHandlerSimple invMaterials;
    public final ItemHandlerSimple invResults;
    private final WorkbenchCrafting crafting;

    public final ItemHandlerSimple resultClient = new ItemHandlerSimple(1);

    public TileAdvancedCraftingTable(BlockPos pos, BlockState state) {
    	super(BCSiliconBlocks.ADVANCED_CRAFTING_TABLE_TILE.get(), pos, state);
        invBlueprint = itemManager.addInvHandler("blueprint", 3 * 3, EnumAccess.PHANTOM);
        invMaterials = itemManager.addInvHandler("materials", 5 * 3, EnumAccess.INSERT, EnumPipePart.VALUES);
        invResults = itemManager.addInvHandler("result", 3 * 3, EnumAccess.EXTRACT, EnumPipePart.VALUES);
        crafting = new WorkbenchCrafting(3, 3, this, invBlueprint, invMaterials, invResults);
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, @Nonnull ItemStack before,
        @Nonnull ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (!ItemStack.isSame(before, after)) {
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
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_GUI_DATA) {
                //resultClient = buffer.readItem();
            }
        }
    }

    @Override
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_GUI_DATA) {
               // buffer.writeItem(crafting.getAssumedResult());
            	resultClient.setStackInSlot(0, crafting.getAssumedResult());
            }
        }
    }
    
	@Override
	public InteractionResult onActivated(Player player, InteractionHand hand, BlockHitResult hit) {
		if(player instanceof ServerPlayer splayer) {
			NetworkHooks.openScreen(splayer, this, worldPosition);
		}
		return super.onActivated(player, hand, hit);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player p_39956_) {
		return new ContainerAdvancedCraftingTable(id, inventory, invMaterials, invResults, invBlueprint, resultClient, ContainerLevelAccess.create(level, worldPosition));
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("TileAdvancedCraftingTable:TODO");//TODO
	}

    public WorkbenchCrafting getWorkbenchCrafting() {
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
}

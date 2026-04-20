/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.pipe.behaviour;

import java.util.List;

import ct.buildcraft.api.BCModules;
import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.transport.pipe.IPipe;
import ct.buildcraft.api.transport.pipe.PipeBehaviour;
import ct.buildcraft.lib.misc.AdvancementUtil;
import ct.buildcraft.lib.tile.item.ItemHandlerSimple;
import ct.buildcraft.transport.client.render.RenderPipeHolder;
import ct.buildcraft.transport.container.ContainerDiamondPipe;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkHooks;

public abstract class PipeBehaviourDiamond extends PipeBehaviour implements MenuProvider{

    public static final int FILTERS_PER_SIDE = 9;
    public static final ResourceLocation ADVANCEMENT_NEED_LIST =
        BCModules.TRANSPORT.createLocation("too_many_pipe_filters");

    public final ItemHandlerSimple filters = new ItemHandlerSimple(FILTERS_PER_SIDE * 6, this::onFilterSlotChange);

    public PipeBehaviourDiamond(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDiamond(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        filters.deserializeNBT(nbt.getCompound("filters"));
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.put("filters", filters.serializeNBT());
        return nbt;
    }

    protected void onFilterSlotChange(IItemHandlerModifiable itemHandler, int slot, ItemStack before, ItemStack after) {
        if (pipe.getHolder().getPipeWorld().isClientSide()) {
            return;
        }
        int baseIndex = FILTERS_PER_SIDE * (slot / FILTERS_PER_SIDE);
        int count = 0;
        for (int i = 0; i < FILTERS_PER_SIDE; i++) {
            int idx = i + baseIndex;
            if (!filters.getStackInSlot(idx).isEmpty()) {
                count++;
            }
        }
        if (count >= FILTERS_PER_SIDE - 2) {
            AdvancementUtil.unlockAdvancement(pipe.getHolder().getOwner().getId(), ADVANCEMENT_NEED_LIST);
        }
    }

    @Override @Deprecated
    public int getTextureIndex(Direction face) {
        return face == null ? 0 : face.ordinal() + 1;
    }
    
	@Override
	public int[] getTextureUVs(Direction face) {
		switch(face) {
		case UP:
			return RenderPipeHolder.UP_UV;
		case DOWN:
			return RenderPipeHolder.DOWN_UV;
		case EAST:
			return RenderPipeHolder.EAST_UV;
		case WEST:
			return RenderPipeHolder.WEST_UV;
		case SOUTH:
			return RenderPipeHolder.SOUTH_UV;
		case NORTH:
			return RenderPipeHolder.NORTH_UV;
		}
		return super.getTextureUVs(face);
	}

    @Override
    public boolean onPipeActivate(Player player, BlockHitResult trace, Level level,
        EnumPipePart part) {
        if (!level.isClientSide()) {
        	NetworkHooks.openScreen((ServerPlayer)player, this, pipe.getHolder().getPipePos());
        }
        return true;
    }
    
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return new ContainerDiamondPipe(id, inventory, filters, this);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(pipe.getDefinition().identifier.toLanguageKey());
	}

	@Override
	public void rotate(Rotation rot) {
		List<ItemStack> copy = List.copyOf(filters.stacks);
		filters.stacks.clear();
		for(Direction face : Direction.values()) {
			Direction rotate = rot.rotate(face);
			for(int i = 0 ; i < FILTERS_PER_SIDE ;i++) {
				filters.stacks.set(i + rotate.ordinal()*FILTERS_PER_SIDE, copy.get(i + face.ordinal()*FILTERS_PER_SIDE));
			}
		}
	}
	
	
}

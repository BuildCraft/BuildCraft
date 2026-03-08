/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.BCModules;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.items.IItemHandlerModifiable;

public abstract class PipeBehaviourDiamond extends PipeBehaviour {

    public static final int FILTERS_PER_SIDE = 9;
    public static final ResourceLocation ADVANCEMENT_NEED_LIST =
            BCModules.TRANSPORT.createLocation("too_many_pipe_filters");

    public final ItemHandlerSimple filters = new ItemHandlerSimple(FILTERS_PER_SIDE * 6, this::onFilterSlotChange);

    public PipeBehaviourDiamond(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDiamond(IPipe pipe, CompoundNBT nbt) {
        super(pipe, nbt);
        filters.deserializeNBT(nbt.getCompound("filters"));
    }

    @Override
    public CompoundNBT writeToNbt() {
        CompoundNBT nbt = super.writeToNbt();
        nbt.put("filters", filters.serializeNBT());
        return nbt;
    }

    protected void onFilterSlotChange(IItemHandlerModifiable itemHandler, int slot, ItemStack before, ItemStack after) {
        if (pipe.getHolder().getPipeWorld().isClientSide) {
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

    @Override
    public int getTextureIndex(Direction face) {
        return face == null ? 0 : face.ordinal() + 1;
    }

    @Override
    public boolean onPipeActivate(PlayerEntity player, RayTraceResult trace, float hitX, float hitY, float hitZ,
                                  EnumPipePart part) {
        if (!player.level.isClientSide) {
//            BCTransportGuis.PIPE_DIAMOND.openGui(player, pipe.getHolder().getPipePos());
            MessageUtil.serverOpenTileGui(player, pipe.getHolder(), pipe.getHolder().getPipePos());
        }
        return true;
    }
}

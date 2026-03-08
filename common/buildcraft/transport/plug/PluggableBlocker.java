/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.plug;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.client.model.key.KeyPlugBlocker;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PluggableBlocker extends PipePluggable {
    private static final VoxelShape[] BOXES = new VoxelShape[6];

    private static final ResourceLocation ADVANCEMENT_PLACE_PLUG = new ResourceLocation(
            "buildcrafttransport:plugging_the_gap"
    );

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;

        double min = 4 / 16.0;
        double max = 12 / 16.0;

        BOXES[Direction.DOWN.ordinal()] = VoxelShapes.box(min, ll, min, max, lu, max);
        BOXES[Direction.UP.ordinal()] = VoxelShapes.box(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.ordinal()] = VoxelShapes.box(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.ordinal()] = VoxelShapes.box(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.ordinal()] = VoxelShapes.box(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.ordinal()] = VoxelShapes.box(ul, min, min, uu, max, max);
    }

    public PluggableBlocker(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        super(definition, holder, side);
    }

    @Override
    public VoxelShape getBoundingBox() {
        return BOXES[side.ordinal()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
        return new ItemStack(BCTransportItems.plugBlocker.get());
    }

    @Override
    public void onPlacedBy(PlayerEntity player) {
        super.onPlacedBy(player);
        if (!holder.getPipeWorld().isClientSide && holder.getPipe().isConnected(side)) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PLACE_PLUG);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public PluggableModelKey getModelRenderKey(RenderType layer) {
        if (layer == RenderType.cutout()) return new KeyPlugBlocker(side);
        return null;
    }
}

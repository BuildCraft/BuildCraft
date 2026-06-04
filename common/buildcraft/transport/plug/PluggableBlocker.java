/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.plug;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.lib.misc.AdvancementUtil;

import buildcraft.transport.client.model.key.KeyPlugBlocker;

public class PluggableBlocker extends PipePluggable {
    private static final Box[] BOXES = new Box[6];

    private static final Identifier ADVANCEMENT_PLACE_PLUG = new Identifier(
        "buildcrafttransport:plugging_the_gap"
    );

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;

        double min = 4 / 16.0;
        double max = 12 / 16.0;

        BOXES[Direction.DOWN.ordinal()]  = new Box(min, ll, min, max, lu, max);
        BOXES[Direction.UP.ordinal()]    = new Box(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.ordinal()] = new Box(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.ordinal()] = new Box(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.ordinal()]  = new Box(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.ordinal()]  = new Box(ul, min, min, uu, max, max);
    }

    public PluggableBlocker(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        super(definition, holder, side);
    }

    @Override
    public Box getBoundingBox() {
        return BOXES[side.ordinal()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
        // STUB(R.Chen): BCTransportItems not in libLeaf (Forge RegistrationHelper dep). Phase 4F.
        return ItemStack.EMPTY;
    }

    @Override
    public void onPlacedBy(PlayerEntity player) {
        super.onPlacedBy(player);
        if (!holder.getPipeWorld().isClient && holder.getPipe().isConnected(side)) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PLACE_PLUG);
        }
    }

    @Override
    public PluggableModelKey getModelRenderKey(RenderLayer layer) {
        if (layer == RenderLayer.getCutout()) return new KeyPlugBlocker(side);
        return null;
    }
}

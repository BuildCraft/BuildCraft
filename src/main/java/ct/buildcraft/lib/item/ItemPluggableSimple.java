/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.transport.IItemPluggable;
import ct.buildcraft.api.transport.pipe.IPipe;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pipe.PipeBehaviour;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableCreator;
import ct.buildcraft.lib.misc.SoundUtil;
import ct.buildcraft.transport.pipe.Pipe;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemPluggableSimple extends Item implements IItemPluggable {

    private static final IPlacementPredicate ALWAYS_CAN = (item, h, s) -> true;

    /** Returns true if the {@link IPipeHolder}'s
     * {@link PipeBehaviour#getCapability(net.minecraftforge.common.capabilities.Capability, Direction)} returns a
     * non-null value for {@link MjAPI#CAP_REDSTONE_RECEIVER}. */
    public static final IPlacementPredicate PIPE_BEHAVIOUR_ACCEPTS_RS_POWER = (item, pipeHolder, side) -> {
        IPipe pipe = pipeHolder.getPipe();
        if (pipe != Pipe.EMPTY) {
            return pipe.getBehaviour().getCapability(MjAPI.CAP_REDSTONE_RECEIVER, side).isPresent();
        }
        return false;
    };

    private final PluggableDefinition definition;
    private final IPlacementPredicate canPlace;
    private final IPluggableCreator creator;

    public ItemPluggableSimple(PluggableDefinition definition, IPluggableCreator creator,
        @Nullable IPlacementPredicate canPlace, Item.Properties p) {
        super(p);
        this.definition = definition;
        this.creator = creator;
        if (creator == null) {
            throw new NullPointerException("Creator was null!");
        }
        this.canPlace = canPlace == null ? ALWAYS_CAN : canPlace;
    }

    public ItemPluggableSimple(PluggableDefinition definition, @Nullable IPlacementPredicate canPlace, Item.Properties p) {
        this(definition, definition.creator, canPlace, p);
    }

    public ItemPluggableSimple(PluggableDefinition definition, @Nonnull IPluggableCreator creator, Item.Properties p) {
        this(definition, creator, null, p);
    }

    public ItemPluggableSimple(PluggableDefinition definition, Item.Properties p) {
        this(definition, definition.creator, null, p);
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, Player player,
        InteractionHand hand) {
        if (!canPlace.canPlace(stack, holder, side)) {
            return PipePluggable.EMPTY;
        }
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos());
        return creator.createSimplePluggable(definition, holder, side);
    }

    @FunctionalInterface
    public interface IPlacementPredicate {
        boolean canPlace(ItemStack stack, IPipeHolder holder, Direction side);
    }
}

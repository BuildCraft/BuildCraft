/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableCreator;
import buildcraft.lib.misc.SoundUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemPluggableSimple extends ItemBC_Neptune implements IItemPluggable {

    private static final IPlacementPredicate ALWAYS_CAN = (item, h, s) -> true;

    /** Returns true if the {@link IPipeHolder}'s
     * {@link PipeBehaviour#getCapability(net.minecraftforge.common.capabilities.Capability, Direction)} returns a
     * non-null value for {@link MjAPI#CAP_REDSTONE_RECEIVER}. */
    public static final IPlacementPredicate PIPE_BEHAVIOUR_ACCEPTS_RS_POWER = (item, pipeHolder, side) ->
    {
        IPipe pipe = pipeHolder.getPipe();
        if (pipe != null) {
            return pipe.getBehaviour().getCapability(MjAPI.CAP_REDSTONE_RECEIVER, side).isPresent();
        }
        return false;
    };

    private final PluggableDefinition definition;
    private final IPlacementPredicate canPlace;
    private final IPluggableCreator creator;

    public ItemPluggableSimple(String idBC, Item.Properties properties, PluggableDefinition definition, IPluggableCreator creator, @Nullable IPlacementPredicate canPlace) {
        super(idBC, properties);
        this.definition = definition;
        this.creator = creator;
        if (creator == null) {
            throw new NullPointerException("Creator was null!");
        }
        this.canPlace = canPlace == null ? ALWAYS_CAN : canPlace;
    }

    public ItemPluggableSimple(String id, Item.Properties properties, PluggableDefinition definition, @Nullable IPlacementPredicate canPlace) {
        this(id, properties, definition, definition.creator, canPlace);
    }

    public ItemPluggableSimple(String id, Item.Properties properties, PluggableDefinition definition, @Nonnull IPluggableCreator creator) {
        this(id, properties, definition, creator, null);
    }

    public ItemPluggableSimple(String id, Item.Properties properties, PluggableDefinition definition) {
        this(id, properties, definition, definition.creator, null);
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, PlayerEntity player, Hand hand) {
        if (!canPlace.canPlace(stack, holder, side)) {
            return null;
        }
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos());
        return creator.createSimplePluggable(definition, holder, side);
    }

    @FunctionalInterface
    public interface IPlacementPredicate {
        boolean canPlace(ItemStack stack, IPipeHolder holder, Direction side);
    }
}

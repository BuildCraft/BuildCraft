/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.item;

import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableCreator;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.SoundUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import javax.annotation.Nonnull;

public class ItemPluggableSimple extends ItemBC_Neptune implements IItemPluggable {

    private final PluggableDefinition definition;

    @Nonnull
    private final IPluggableCreator creator;

    public ItemPluggableSimple(String id, PluggableDefinition definition, @Nonnull IPluggableCreator creator) {
        super(id);
        this.definition = definition;
        this.creator = creator;
    }

    public ItemPluggableSimple(String id, PluggableDefinition definition) {
        this(id, definition, definition.creator);
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, EnumFacing side, EntityPlayer player, EnumHand hand) {
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos());
        return creator.createSimplePluggable(definition, holder, side);
    }
}

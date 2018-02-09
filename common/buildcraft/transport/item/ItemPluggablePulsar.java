/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.item;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.SoundUtil;

import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.plug.PluggablePulsar;

@Deprecated
public class ItemPluggablePulsar extends ItemBC_Neptune implements IItemPluggable {
    public ItemPluggablePulsar(String id) {
        super(id);
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, EnumFacing side, EntityPlayer player,
        EnumHand hand) {
        IPipe pipe = holder.getPipe();
        if (pipe == null) {
            return null;
        }
        PipeBehaviour behaviour = pipe.getBehaviour();
        if (behaviour instanceof IMjRedstoneReceiver) {
            SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos());
            return new PluggablePulsar(BCTransportPlugs.pulsar, holder, side);
        } else {
            return null;
        }
    }
}

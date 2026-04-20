/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.item;

import javax.annotation.Nonnull;

import ct.buildcraft.api.mj.IMjRedstoneReceiver;
import ct.buildcraft.api.transport.IItemPluggable;
import ct.buildcraft.api.transport.pipe.IPipe;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pipe.PipeBehaviour;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.lib.misc.SoundUtil;
import ct.buildcraft.silicon.BCSiliconPlugs;
import ct.buildcraft.silicon.plug.PluggablePulsar;
import ct.buildcraft.transport.pipe.Pipe;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Deprecated
public class ItemPluggablePulsar extends Item implements IItemPluggable {
    public ItemPluggablePulsar() {
        super(new Item.Properties());
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, Player player,
        InteractionHand hand) {
        IPipe pipe = holder.getPipe();
        if (pipe == Pipe.EMPTY) {
            return PipePluggable.EMPTY;
        }
        PipeBehaviour behaviour = pipe.getBehaviour();
        if (behaviour instanceof IMjRedstoneReceiver) {
            SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos());
            return new PluggablePulsar(BCSiliconPlugs.pulsar, holder, side);
        } else {
            return PipePluggable.EMPTY;
        }
    }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.*;
import net.minecraft.nbt.CompoundNBT;

import java.util.Arrays;

public class PipeBehaviourVoid extends PipeBehaviour {
    public PipeBehaviourVoid(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourVoid(IPipe pipe, CompoundNBT nbt) {
        super(pipe, nbt);
    }

    @PipeEventHandler
    public static void reachCentre(PipeEventItem.ReachCenter reachCenter) {
        reachCenter.getStack().setCount(0);
    }

    @PipeEventHandler
    public static void moveFluidToCentre(PipeEventFluid.OnMoveToCentre move) {
        /*
        int removed = 0;
        for (int i = 0; i < move.fluidEnteringCentre.length; i++) {
            removed += move.fluidEnteringCentre[i];
        }
        */
        Arrays.fill(move.fluidEnteringCentre, 0);
        /*
        World world = move.holder.getPipeWorld();
        BlockPos pos = move.holder.getPipePos();
        if (removed > 0 && (world.getTotalWorldTime() + pos.toLong()) % 23 == 0) {
            SoundType soundType = SoundType.SLIME;
            final SoundEvent soundEvent;
            Fluid f = move.fluid.getFluid();
            if (f == FluidRegistry.LAVA) {
                soundEvent = SoundEvents.ITEM_BUCKET_EMPTY_LAVA;
            } else if (f == FluidRegistry.WATER) {
                soundEvent = SoundEvents.ITEM_BUCKET_EMPTY;
            } else {
                soundEvent = SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH;
            }
            float volume = (soundType.getVolume() + 1.0F) / 5.0F;
            float pitch = soundType.getPitch() * 0.1F;
            world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, volume, pitch);
        }
        */
    }
}

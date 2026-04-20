/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.CompoundTag;

import ct.buildcraft.api.transport.pipe.IPipe;
import ct.buildcraft.api.transport.pipe.PipeEventHandler;
import ct.buildcraft.api.transport.pipe.PipeEventItem;

public class PipeBehaviourCobble extends PipeBehaviourSeparate {
    private static final double SPEED_DELTA = 0.02;
    private static final double SPEED_TARGET = 0.01;

    public PipeBehaviourCobble(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourCobble(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
    }

    @PipeEventHandler
    public static void modifySpeed(PipeEventItem.ModifySpeed event) {
        event.modifyTo(SPEED_TARGET, SPEED_DELTA);
    }
}

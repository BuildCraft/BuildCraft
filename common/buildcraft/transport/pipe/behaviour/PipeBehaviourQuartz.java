/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import net.minecraft.nbt.CompoundNBT;

public class PipeBehaviourQuartz extends PipeBehaviourSeparate {
    private static final double SPEED_DELTA = 0.002;
    private static final double SPEED_TARGET = 0.01;

    public PipeBehaviourQuartz(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourQuartz(IPipe pipe, CompoundNBT nbt) {
        super(pipe, nbt);
    }

    @PipeEventHandler
    public static void modifySpeed(PipeEventItem.ModifySpeed event) {
        event.modifyTo(SPEED_TARGET, SPEED_DELTA);
    }
}

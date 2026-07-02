/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.pipe.behaviour;

import ct.buildcraft.api.transport.pipe.IPipe;
import ct.buildcraft.api.transport.pipe.IPipe.ConnectedType;
import ct.buildcraft.api.transport.pipe.PipeBehaviour;
import ct.buildcraft.api.transport.pipe.PipeEventFluid;
import ct.buildcraft.api.transport.pipe.PipeEventHandler;
import ct.buildcraft.api.transport.pipe.PipeEventItem;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class PipeBehaviourClay extends PipeBehaviour {
    public PipeBehaviourClay(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourClay(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
    }

    @PipeEventHandler
    public void orderSides(PipeEventItem.SideCheck ordering) {
        for (Direction face : Direction.values()) {
            ConnectedType type = pipe.getConnectedType(face);
            if (type == ConnectedType.TILE) {
                /* We only really need to increase the priority, but using a larger number (100) means that it doesn't
                 * matter what plugs are attached (e.g. filters) and this will always prefer to go into inventories
                 * above the correct filters. (Although note that the filters still matter) */
                ordering.increasePriority(face, 100);
            }
        }
    }

    @PipeEventHandler
    public void orderSides(PipeEventFluid.SideCheck ordering) {
        for (Direction face : Direction.values()) {
            ConnectedType type = pipe.getConnectedType(face);
            if (type == ConnectedType.TILE) {
                /* We only really need to increase the priority, but using a larger number (100) means that it doesn't
                 * matter what plugs are attached (e.g. filters) and this will always prefer to go into inventories
                 * above the correct filters. (Although note that the filters still matter) */
                ordering.increasePriority(face, 100);
            }
        }
    }
}

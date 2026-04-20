/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.plug;

import ct.buildcraft.api.transport.pipe.IPipe;
import ct.buildcraft.api.transport.pipe.PipeEventHandler;
import ct.buildcraft.api.transport.pipe.PipeEventItem;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.transport.pipe.Pipe;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public class FilterEventHandler {
    @PipeEventHandler
    public static void sideCheck(PipeEventItem.SideCheck event) {
        for (Direction side : Direction.values()) {
            if (!event.isAllowed(side)) {
                continue;
            }
            IPipe neighbour = event.holder.getNeighbourPipe(side);
            if (neighbour == Pipe.EMPTY) {
                continue;
            }
            PipePluggable neighbourPlug = neighbour.getHolder().getPluggable(side.getOpposite());
            PipePluggable atPlug = event.holder.getPluggable(side);
            if (neighbourPlug instanceof PluggableLens) {
                DyeColor colourAt = event.colour;
                if (atPlug instanceof PluggableLens) {
                    PluggableLens lens = (PluggableLens) atPlug;
                    if (!lens.isFilter) {
                        colourAt = lens.colour;
                    }
                }

                PluggableLens lens = (PluggableLens) neighbourPlug;
                if (lens.isFilter) {
                    if (colourAt == lens.colour) {
                        event.increasePriority(side);
                    } else if (colourAt == null) {
                        event.decreasePriority(side);
                    }
                }
            }
        }
    }
}

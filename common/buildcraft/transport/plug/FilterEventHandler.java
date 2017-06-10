/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.plug;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pluggable.PipePluggable;

public class FilterEventHandler {
    @PipeEventHandler
    public static void sideCheck(PipeEventItem.SideCheck event) {
        for (EnumFacing side : EnumFacing.VALUES) {
            if (!event.isAllowed(side)) {
                continue;
            }
            IPipe neighbour = event.holder.getNeighbourPipe(side);
            if (neighbour == null) {
                continue;
            }
            PipePluggable neighbourPlug = neighbour.getHolder().getPluggable(side.getOpposite());
            PipePluggable atPlug = event.holder.getPluggable(side);
            if (neighbourPlug instanceof PluggableLens) {
                EnumDyeColor colourAt = event.colour;
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

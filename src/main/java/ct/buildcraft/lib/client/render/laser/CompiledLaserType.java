/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.render.laser;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserRow;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserSide;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;

import net.minecraft.util.Mth;


public class CompiledLaserType {
    public final LaserType type;
    private final CompiledLaserRow startCap, endCap;
    private final CompiledLaserRow start, end;
    private final double startWidth, middleWidth, endWidth;
    private final Map<LaserSide, CompiledLaserRow> rows = new EnumMap<>(LaserSide.class);

    public CompiledLaserType(LaserType type) {
        this.type = type;
        this.startCap = new CompiledLaserRow(type.capStart);
        this.endCap = new CompiledLaserRow(type.capEnd);
        this.start = type.start == null ? null : new CompiledLaserRow(type.start);
        this.end = type.end == null ? null : new CompiledLaserRow(type.end);
        this.startWidth = start == null ? 0 : start.width;
        this.endWidth = end == null ? 0 : end.width;
        for (LaserSide side : LaserSide.VALUES) {
            List<LaserRow> validRows = new ArrayList<>();
            for (LaserRow row : type.variations) {
                for (LaserSide inner : row.validSides) {
                    if (inner == side) {
                        validRows.add(row);
                        break;
                    }
                }
            }
            rows.put(side, new CompiledLaserRow(validRows.toArray(new LaserRow[validRows.size()])));
        }
        this.middleWidth = rows.get(LaserSide.BOTTOM).width;
    }

    public void bakeFor(LaserContext context) {
        startCap.bakeStartCap(context);
        endCap.bakeEndCap(context);

        double lengthForMiddle = Math.max(0, context.length - startWidth - endWidth);
        int numMiddle = Mth.floor(lengthForMiddle / middleWidth);
        double leftOverFromMiddle = lengthForMiddle - middleWidth * numMiddle;
        if (leftOverFromMiddle > 0) {
            numMiddle++;
        }
        double lengthEnds = context.length - middleWidth * numMiddle;
        final double startLength, endLength;
        if (startWidth > 0 && endWidth > 0) {
            double ratioStartEnd = startWidth / endWidth;
            startLength = (lengthEnds / 2) * ratioStartEnd;
            endLength = (lengthEnds / 2) / ratioStartEnd;
        } else if (startWidth <= 0) {
            startLength = 0;
            endLength = lengthEnds;
        } else {// endWidth <= 0
            startLength = lengthEnds;
            endLength = 0;
        }
        if (startLength > 0) start.bakeStart(context, startLength);
        if (endLength > 0) end.bakeEnd(context, endLength);

        if (numMiddle > 0) {
            for (LaserSide side : LaserSide.VALUES) {
                CompiledLaserRow interp = rows.get(side);
                interp.bakeFor(context, side, startLength, numMiddle);
            }
        }
    }
}

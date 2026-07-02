/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.render.laser;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserRow;
import ct.buildcraft.lib.client.render.laser.LaserData_BC8.LaserSide;

public class CompiledLaserRow {
    public final LaserRow[] rows;
    private final ISprite[] sprites;
    public final double width;
    public final double height;
    private int currentRowIndex;

    public CompiledLaserRow(LaserRow row) {
        this(new LaserRow[] { row });
    }

    public CompiledLaserRow(LaserRow[] rows) {
        if (rows.length < 1) throw new IllegalArgumentException("Not enough rows!");
        this.rows = rows;
        this.width = rows[0].width;
        this.height = rows[0].height;
        this.sprites = new ISprite[rows.length];
        for (int i = 0; i < rows.length; i++) {
            sprites[i] = rows[i].sprite;
        }
    }

    private double texU(double between) {
    	ISprite sprite = sprites[currentRowIndex];
        LaserRow row = rows[currentRowIndex];
        if (between == 0) return sprite.getInterpU(row.uMin);
        if (between == 1) return sprite.getInterpU(row.uMax);
        double interp = row.uMin * (1 - between) + row.uMax * between;
        return sprite.getInterpU(interp);
    }

    private double texV(double between) {
    	ISprite sprite = sprites[currentRowIndex];
        LaserRow row = rows[currentRowIndex];
        if (between == 0) return sprite.getInterpV(row.vMin);
        if (between == 1) return sprite.getInterpV(row.vMax);
        double interp = row.vMin * (1 - between) + row.vMax * between;
        return sprite.getInterpV(interp);
    }

    public void bakeStartCap(LaserContext context) {
        this.currentRowIndex = 0;
        double h = height / 2;
        context.setFaceNormal(-1, 0, 0);
        context.addPoint(0, h, h, texU(1), texV(1));
        context.addPoint(0, h, -h, texU(1), texV(0));
        context.addPoint(0, -h, -h, texU(0), texV(0));
        context.addPoint(0, -h, h, texU(0), texV(1));
    }

    public void bakeEndCap(LaserContext context) {
        this.currentRowIndex = 0;
        double h = height / 2;
        context.setFaceNormal(1, 0, 0);
        context.addPoint(context.length, -h, h, texU(0), texV(1));
        context.addPoint(context.length, -h, -h, texU(0), texV(0));
        context.addPoint(context.length, h, -h, texU(1), texV(0));
        context.addPoint(context.length, h, h, texU(1), texV(1));
    }

    public void bakeStart(LaserContext context, double length) {
        this.currentRowIndex = 0;
        final double h = height / 2;
        final double l = length;
        final double i = 1 - (length / width);
        // TOP
        context.setFaceNormal(0, 1, 0);
        context.addPoint(0, h, -h, texU(i), texV(0));// 1
        context.addPoint(0, h, h, texU(i), texV(1));// 2
        context.addPoint(l, h, h, texU(1), texV(1));// 3
        context.addPoint(l, h, -h, texU(1), texV(0));// 4
        // BOTTOM
        context.setFaceNormal(0, -1, 0);
        context.addPoint(l, -h, -h, texU(1), texV(0));// 4
        context.addPoint(l, -h, h, texU(1), texV(1));// 3
        context.addPoint(0, -h, h, texU(i), texV(1));// 2
        context.addPoint(0, -h, -h, texU(i), texV(0));// 1
        // LEFT
        context.setFaceNormal(0, 0, -1);
        context.addPoint(0, -h, -h, texU(i), texV(0));// 1
        context.addPoint(0, h, -h, texU(i), texV(1));// 2
        context.addPoint(l, h, -h, texU(1), texV(1));// 3
        context.addPoint(l, -h, -h, texU(1), texV(0));// 4
        // RIGHT
        context.setFaceNormal(0, 0, 1);
        context.addPoint(l, -h, h, texU(1), texV(0));// 4
        context.addPoint(l, h, h, texU(1), texV(1));// 3
        context.addPoint(0, h, h, texU(i), texV(1));// 2
        context.addPoint(0, -h, h, texU(i), texV(0));// 1
    }

    public void bakeEnd(LaserContext context, double length) {
        this.currentRowIndex = 0;
        final double h = height / 2;
        final double ls = context.length - length;
        final double lb = context.length;
        final double i = length / width;
        // TOP
        context.setFaceNormal(0, 1, 0);
        context.addPoint(ls, h, -h, texU(0), texV(0));// 1
        context.addPoint(ls, h, h, texU(0), texV(1));// 2
        context.addPoint(lb, h, h, texU(i), texV(1));// 3
        context.addPoint(lb, h, -h, texU(i), texV(0));// 4
        // BOTTOM
        context.setFaceNormal(0, -1, 0);
        context.addPoint(lb, -h, -h, texU(i), texV(0));// 4
        context.addPoint(lb, -h, h, texU(i), texV(1));// 3
        context.addPoint(ls, -h, h, texU(0), texV(1));// 2
        context.addPoint(ls, -h, -h, texU(0), texV(0));// 1
        // LEFT
        context.setFaceNormal(0, 0, -1);
        context.addPoint(ls, -h, -h, texU(0), texV(0));// 1
        context.addPoint(ls, h, -h, texU(0), texV(1));// 2
        context.addPoint(lb, h, -h, texU(i), texV(1));// 3
        context.addPoint(lb, -h, -h, texU(i), texV(0));// 4
        // RIGHT
        context.setFaceNormal(0, 0, 1);
        context.addPoint(lb, -h, h, texU(i), texV(0));// 4
        context.addPoint(lb, h, h, texU(i), texV(1));// 3
        context.addPoint(ls, h, h, texU(0), texV(1));// 2
        context.addPoint(ls, -h, h, texU(0), texV(0));// 1
    }

    public void bakeFor(LaserContext context, LaserSide side, double startX, int count) {
        double xMin = startX;
        double xMax = startX + width;
        double h = height / 2;
        for (int i = 0; i < count; i++) {
            this.currentRowIndex = i % rows.length;
            double ls = xMin;
            double lb = xMax;
            if (side == LaserSide.TOP) {
                context.setFaceNormal(0, 1, 0);
                context.addPoint(ls, h, -h, texU(0), texV(0));// 1
                context.addPoint(ls, h, h, texU(0), texV(1));// 2
                context.addPoint(lb, h, h, texU(1), texV(1));// 3
                context.addPoint(lb, h, -h, texU(1), texV(0));// 4
            } else if (side == LaserSide.BOTTOM) {
                context.setFaceNormal(0, -1, 0);
                context.addPoint(lb, -h, -h, texU(1), texV(0));// 4
                context.addPoint(lb, -h, h, texU(1), texV(1));// 3
                context.addPoint(ls, -h, h, texU(0), texV(1));// 2
                context.addPoint(ls, -h, -h, texU(0), texV(0));// 1
            } else if (side == LaserSide.LEFT) {
                context.setFaceNormal(0, 0, -1);
                context.addPoint(ls, -h, -h, texU(0), texV(0));// 1
                context.addPoint(ls, h, -h, texU(0), texV(1));// 2
                context.addPoint(lb, h, -h, texU(1), texV(1));// 3
                context.addPoint(lb, -h, -h, texU(1), texV(0));// 4
            } else if (side == LaserSide.RIGHT) {
                context.setFaceNormal(0, 0, 1);
                context.addPoint(lb, -h, h, texU(1), texV(0));// 4
                context.addPoint(lb, h, h, texU(1), texV(1));// 3
                context.addPoint(ls, h, h, texU(0), texV(1));// 2
                context.addPoint(ls, -h, h, texU(0), texV(0));// 1
            }
            xMin += width;
            xMax += width;
        }
    }
}

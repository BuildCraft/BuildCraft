package buildcraft.lib.client.render.laser;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import buildcraft.lib.client.render.laser.LaserData_BC8.LaserRow;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserSide;

public class CompiledLaserRow {
    public final LaserRow[] rows;
    private final TextureAtlasSprite[] sprites;
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
        this.sprites = new TextureAtlasSprite[rows.length];
        for (int i = 0; i < rows.length; i++) {
            sprites[i] = rows[i].sprite.getSprite();
        }
    }

    private double texU(double between) {
        TextureAtlasSprite sprite = sprites[currentRowIndex];
        LaserRow row = rows[currentRowIndex];
        if (between == 0) return sprite.getInterpolatedU(row.uMin);
        if (between == 1) return sprite.getInterpolatedU(row.uMax);
        double interp = row.uMin * (1 - between) + row.uMax * between;
        return sprite.getInterpolatedU(interp);
    }

    private double texV(double between) {
        TextureAtlasSprite sprite = sprites[currentRowIndex];
        LaserRow row = rows[currentRowIndex];
        if (between == 0) return sprite.getInterpolatedV(row.vMin);
        if (between == 1) return sprite.getInterpolatedV(row.vMax);
        double interp = row.vMin * (1 - between) + row.vMax * between;
        return sprite.getInterpolatedV(interp);
    }

    public void bakeStartCap(LaserContext context) {
        this.currentRowIndex = 0;
        double h = height / 2;
        context.addPoint(0, h, h, texU(1), texV(1));
        context.addPoint(0, h, -h, texU(1), texV(0));
        context.addPoint(0, -h, -h, texU(0), texV(0));
        context.addPoint(0, -h, h, texU(0), texV(1));
    }

    public void bakeEndCap(LaserContext context) {
        this.currentRowIndex = 0;
        double h = height / 2;
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
        context.addPoint(0, h, -h, texU(i), texV(0));// 1
        context.addPoint(0, h, h, texU(i), texV(1));// 2
        context.addPoint(l, h, h, texU(1), texV(1));// 3
        context.addPoint(l, h, -h, texU(1), texV(0));// 4
        // BOTTOM
        context.addPoint(l, -h, -h, texU(1), texV(0));// 4
        context.addPoint(l, -h, h, texU(1), texV(1));// 3
        context.addPoint(0, -h, h, texU(i), texV(1));// 2
        context.addPoint(0, -h, -h, texU(i), texV(0));// 1
        // LEFT
        context.addPoint(0, -h, -h, texU(i), texV(0));// 1
        context.addPoint(0, h, -h, texU(i), texV(1));// 2
        context.addPoint(l, h, -h, texU(1), texV(1));// 3
        context.addPoint(l, -h, -h, texU(1), texV(0));// 4
        // RIGHT
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
        context.addPoint(ls, h, -h, texU(0), texV(0));// 1
        context.addPoint(ls, h, h, texU(0), texV(1));// 2
        context.addPoint(lb, h, h, texU(i), texV(1));// 3
        context.addPoint(lb, h, -h, texU(i), texV(0));// 4
        // BOTTOM
        context.addPoint(lb, -h, -h, texU(i), texV(0));// 4
        context.addPoint(lb, -h, h, texU(i), texV(1));// 3
        context.addPoint(ls, -h, h, texU(0), texV(1));// 2
        context.addPoint(ls, -h, -h, texU(0), texV(0));// 1
        // LEFT
        context.addPoint(ls, -h, -h, texU(0), texV(0));// 1
        context.addPoint(ls, h, -h, texU(0), texV(1));// 2
        context.addPoint(lb, h, -h, texU(i), texV(1));// 3
        context.addPoint(lb, -h, -h, texU(i), texV(0));// 4
        // RIGHT
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
                context.addPoint(ls, h, -h, texU(0), texV(0));// 1
                context.addPoint(ls, h, h, texU(0), texV(1));// 2
                context.addPoint(lb, h, h, texU(1), texV(1));// 3
                context.addPoint(lb, h, -h, texU(1), texV(0));// 4
            } else if (side == LaserSide.BOTTOM) {
                context.addPoint(lb, -h, -h, texU(1), texV(0));// 4
                context.addPoint(lb, -h, h, texU(1), texV(1));// 3
                context.addPoint(ls, -h, h, texU(0), texV(1));// 2
                context.addPoint(ls, -h, -h, texU(0), texV(0));// 1
            } else if (side == LaserSide.LEFT) {
                context.addPoint(ls, -h, -h, texU(0), texV(0));// 1
                context.addPoint(ls, h, -h, texU(0), texV(1));// 2
                context.addPoint(lb, h, -h, texU(1), texV(1));// 3
                context.addPoint(lb, -h, -h, texU(1), texV(0));// 4
            } else if (side == LaserSide.RIGHT) {
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
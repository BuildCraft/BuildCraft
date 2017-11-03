package buildcraft.core.patterns;

import buildcraft.api.core.render.ISprite;

import buildcraft.core.BCCoreSprites;

public class PatternShape2dTriangle extends PatternShape2d {
    public PatternShape2dTriangle() {
        super("2d_triangle");
    }

    @Override
    public ISprite getSprite() {
        return BCCoreSprites.FILLER_2D_TRIANGLE;
    }

    @Override
    protected void genShape(int maxA, int maxB, LineList list) {
        int halfA = maxA / 2;
        list.moveTo(maxA, maxB);
        list.lineTo(0, maxB);
        list.lineTo(halfA, 0);
        list.moveTo(maxA - halfA, 0);
        list.lineFrom(maxA, maxB);
        list.setFillPoint(halfA, maxB / 2);
    }
}

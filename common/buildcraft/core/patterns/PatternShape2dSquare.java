package buildcraft.core.patterns;

import buildcraft.api.core.render.ISprite;

import buildcraft.core.BCCoreSprites;

public class PatternShape2dSquare extends PatternShape2d {

    public PatternShape2dSquare() {
        super("2d_square");
    }

    @Override
    public int minParameters() {
        return 2;
    }

    @Override
    public int maxParameters() {
        return 2;
    }

    @Override
    public ISprite getSprite() {
        return BCCoreSprites.FILLER_2D_SQUARE;
    }

    @Override
    protected void genShape(int maxA, int maxB, LineList list) {
        list.lineTo(maxA, 0);
        list.lineTo(maxA, maxB);
        list.lineTo(0, maxB);
        list.lineTo(0, 0);
        list.setFillPoint(maxA / 2, maxB / 2);
    }
}

package buildcraft.core.builders.patterns;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.BCLibSprites;

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
        return BCLibSprites.HELP_SPLIT;
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

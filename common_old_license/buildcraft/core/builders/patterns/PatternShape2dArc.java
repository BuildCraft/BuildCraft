package buildcraft.core.builders.patterns;

import buildcraft.api.core.render.ISprite;

import buildcraft.core.BCCoreSprites;

public class PatternShape2dArc extends PatternShape2d {

    public PatternShape2dArc() {
        super("2d_arc");
    }

    @Override
    public ISprite getSprite() {
        return BCCoreSprites.FILLER_2D_ARC;
    }

    @Override
    protected void genShape(int maxA, int maxB, LineList list) {
        list.setFillPoint(maxA, maxB);
        list.arc(maxA, maxB, maxA, maxB);
    }
}

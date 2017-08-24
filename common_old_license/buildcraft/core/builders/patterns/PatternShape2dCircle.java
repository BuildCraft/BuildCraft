package buildcraft.core.builders.patterns;

import buildcraft.api.core.render.ISprite;

import buildcraft.core.BCCoreSprites;

public class PatternShape2dCircle extends PatternShape2d {

    public PatternShape2dCircle() {
        super("circle");
    }

    @Override
    public ISprite getSprite() {
        return BCCoreSprites.FILLER_2D_CIRCLE;
    }

    @Override
    protected void genShape(int maxA, int maxB, LineList list) {
//        list.moveTo
    }
}

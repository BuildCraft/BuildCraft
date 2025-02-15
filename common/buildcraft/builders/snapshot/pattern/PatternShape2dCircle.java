package buildcraft.builders.snapshot.pattern;

import buildcraft.api.core.render.ISprite;
import buildcraft.builders.BCBuildersSprites;

public class PatternShape2dCircle extends PatternShape2d {
    public PatternShape2dCircle() {
        super("2d_circle");
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.FILLER_2D_CIRCLE;
    }

    @Override
    protected void genShape(int maxA, int maxB, LineList list) {
        if (maxA == 0 || maxB == 0) {
            list.moveTo(0, 0);
            list.lineTo(maxA, maxB);
            return;
        }
        int halfA = maxA / 2;
        int halfB = maxB / 2;
        int halfAUpper = maxA - halfA;
        int halfBUpper = maxB - halfB;
        list.setFillPoint(halfA, halfB);
        list.arc(halfA, halfB, maxA / 2.0, maxB / 2.0, halfAUpper - halfA, halfBUpper - halfB, ArcType.FULL_CIRCLE);
    }
}

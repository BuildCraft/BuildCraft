package ct.buildcraft.builders.snapshot.pattern;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.builders.BCBuildersSprites;

public class PatternShape2dSemiCircle extends PatternShape2d {
    public PatternShape2dSemiCircle() {
        super("2d_semi_circle");
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.FILLER_2D_SEMI_CIRCLE;
    }

    @Override
    protected void genShape(int maxA, int maxB, LineList list) {
        if (maxA == 0 || maxB == 0) {
            list.moveTo(0, 0);
            list.lineTo(maxA, maxB);
            return;
        }
        int halfA = maxA / 2;
        int halfAUpper = maxA - halfA;
        list.setFillPoint(halfA, maxB);
        list.arc(halfA, maxB, maxA / 2.0, maxB, halfAUpper - halfA, 0, ArcType.SEMI_CIRCLE);
    }
}

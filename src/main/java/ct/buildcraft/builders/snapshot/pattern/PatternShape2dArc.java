package ct.buildcraft.builders.snapshot.pattern;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.builders.BCBuildersSprites;

public class PatternShape2dArc extends PatternShape2d {
    public PatternShape2dArc() {
        super("2d_arc");
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.FILLER_2D_ARC;
    }

    @Override
    protected void genShape(int maxA, int maxB, LineList list) {
        if (maxA == 0 || maxB == 0) {
            list.moveTo(0, 0);
            list.lineTo(maxA, maxB);
            return;
        }
        list.setFillPoint(maxA, maxB);
        list.arc(maxA, maxB, maxA, maxB);
    }
}

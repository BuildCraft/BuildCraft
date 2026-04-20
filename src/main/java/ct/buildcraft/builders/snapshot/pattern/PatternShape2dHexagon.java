package ct.buildcraft.builders.snapshot.pattern;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.builders.BCBuildersSprites;

public class PatternShape2dHexagon extends PatternShape2d {
    public PatternShape2dHexagon() {
        super("2d_hexagon");
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.FILLER_2D_HEXAGON;
    }

    @Override
    protected void genShape(int maxA, int maxB, LineList list) {
        int indent = maxA / 4;
        int halfB = maxB / 2;
        list.moveTo(indent, 0);
        list.lineTo(maxA - indent, 0);
        list.lineFrom(maxA, halfB);
        list.moveTo(maxA, maxB - halfB);
        list.lineTo(maxA - indent, maxB);
        list.lineFrom(indent, maxB);
        list.lineFrom(0, maxB - halfB);
        list.moveTo(0, halfB);
        list.lineTo(indent, 0);
    }
}

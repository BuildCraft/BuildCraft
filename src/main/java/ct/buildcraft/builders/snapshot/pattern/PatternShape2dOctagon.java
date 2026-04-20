package ct.buildcraft.builders.snapshot.pattern;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.builders.BCBuildersSprites;

public class PatternShape2dOctagon extends PatternShape2d {
    public PatternShape2dOctagon() {
        super("2d_octagon");
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
        return BCBuildersSprites.FILLER_2D_OCTAGON;
    }

    @Override
    protected void genShape(int maxA, int maxB, LineList list) {

    }
}

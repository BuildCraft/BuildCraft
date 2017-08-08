package buildcraft.core.builders.patterns;

import buildcraft.api.core.render.ISprite;

import buildcraft.core.BCCoreSprites;

public class PatternShape2dOctagon extends PatternShape2d {

    public PatternShape2dOctagon() {
        super("2d_ocotagon");
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
        return BCCoreSprites.TRIGGER_POWER_LOW;
    }

    @Override
    protected void genShape(int maxA, int maxB, LineList list) {

    }
}

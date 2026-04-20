package ct.buildcraft.builders.snapshot.pattern;

import ct.buildcraft.api.filler.IFilledTemplate;
import ct.buildcraft.api.filler.IFillerPatternShape;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.builders.BCBuildersSprites;
import ct.buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PatternNone extends Pattern implements IFillerPatternShape {
    public PatternNone() {
        super("none");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public SpriteHolder getSprite() {
        return BCBuildersSprites.FILLER_NONE;
    }

    @Override
    public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
        return false;
    }
}

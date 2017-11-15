package buildcraft.builders.snapshot.pattern;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPatternShape;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import buildcraft.builders.BCBuildersSprites;

public class PatternNone extends Pattern implements IFillerPatternShape {
    public PatternNone() {
        super("none");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getSprite() {
        return BCBuildersSprites.FILLER_NONE;
    }

    @Override
    public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
        return false;
    }
}

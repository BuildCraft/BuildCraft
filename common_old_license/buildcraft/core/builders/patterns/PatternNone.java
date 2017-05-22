package buildcraft.core.builders.patterns;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import buildcraft.core.BCCoreSprites;

public class PatternNone extends Pattern {
    public PatternNone() {
        super("none");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getGuiSprite() {
        return BCCoreSprites.FILLER_NONE;
    }

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        return null;
    }
}

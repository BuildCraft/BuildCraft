package buildcraft.core.patterns;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.BCCoreStatements;
import buildcraft.core.statements.BCStatement;

public abstract class Pattern extends BCStatement implements IFillerPattern, IActionExternal {
    private final String desc;

    public Pattern(String tag) {
        super("buildcraft:" + tag);
        desc = "fillerpattern." + tag;
        FillerManager.registry.addPattern(this);
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize(desc);
    }

    @Override
    public void actionActivate(TileEntity target, EnumFacing side, IStatementContainer source, IStatementParameter[] parameters) {
        if (source instanceof IFillerStatementContainer) {
            ((IFillerStatementContainer) source).setPattern(this, parameters);
        } else if (target instanceof IFillerStatementContainer) {
            ((IFillerStatementContainer) target).setPattern(this, parameters);
        }
    }

    @Override
    public IFillerPattern[] getPossible() {
        return BCCoreStatements.PATTERNS;
    }
}

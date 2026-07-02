package ct.buildcraft.builders.snapshot.pattern;

import ct.buildcraft.api.filler.FillerManager;
import ct.buildcraft.api.filler.IFillerPattern;
import ct.buildcraft.api.statements.IActionExternal;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.containers.IFillerStatementContainer;
import ct.buildcraft.builders.BCBuildersStatements;
import ct.buildcraft.core.statements.BCStatement;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class Pattern extends BCStatement implements IFillerPattern, IActionExternal {
    private final String desc;

    public Pattern(String tag) {
        super("buildcraft:" + tag);
        desc = "fillerpattern." + tag;
        FillerManager.registry.addPattern(this);
    }

    @Override
    public Component getDescription() {
        return Component.translatable(desc);
    }

    @Override
    public void actionActivate(BlockEntity target, Direction side, IStatementContainer source, IStatementParameter[] parameters) {
        if (source instanceof IFillerStatementContainer) {
            ((IFillerStatementContainer) source).setPattern(this, parameters);
        } else if (target instanceof IFillerStatementContainer) {
            ((IFillerStatementContainer) target).setPattern(this, parameters);
        }
    }

    @Override
    public IFillerPattern[] getPossible() {
        return BCBuildersStatements.PATTERNS;
    }
}

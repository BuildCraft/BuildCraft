package buildcraft.core.builders.patterns;

import net.minecraft.world.World;

import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.Box;
import buildcraft.core.blueprints.Template;

public class PatternNone extends FillerPattern {

    public static final PatternNone INSTANCE = new PatternNone();

    public PatternNone() {
        super("none", EnumFillerPattern.NONE);
    }

    @Override
    public Template getTemplate(Box box, World world, IStatementParameter[] parameters) {
        return new Template(0, 0, 0);
    }

}

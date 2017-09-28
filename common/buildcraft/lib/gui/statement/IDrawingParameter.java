package buildcraft.lib.gui.statement;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.statements.IStatementParameter;

import buildcraft.lib.gui.ISimpleDrawable;

/** An {@link IStatementParameter} that provides methods to draw itself. */
public interface IDrawingParameter extends IStatementParameter {
    @SideOnly(Side.CLIENT)
    ISimpleDrawable getDrawable();
}

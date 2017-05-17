package buildcraft.lib.gui.statement;

import java.util.Arrays;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;

public abstract class StatementWrapper implements IStatement, Comparable<StatementWrapper> {
    public final IStatement delegate;

    /** Used to determine the background colour of triggers and actions. */
    public final EnumPipePart sourcePart;

    public StatementWrapper(IStatement delegate, EnumPipePart sourcePart) {
        this.delegate = delegate;
        this.sourcePart = sourcePart;
    }

    /** @see buildcraft.api.statements.IStatement#getUniqueTag() */
    @Override
    public String getUniqueTag() {
        return this.delegate.getUniqueTag();
    }

    /** @see buildcraft.api.statements.IStatement#maxParameters() */
    @Override
    public int maxParameters() {
        return this.delegate.maxParameters();
    }

    /** @see buildcraft.api.statements.IStatement#minParameters() */
    @Override
    public int minParameters() {
        return this.delegate.minParameters();
    }

    /** @see buildcraft.api.statements.IStatement#getDescription() */
    @Override
    public String getDescription() {
        return this.delegate.getDescription();
    }

    /** @see buildcraft.api.statements.IStatement#createParameter(int) */
    @Override
    public IStatementParameter createParameter(int index) {
        return this.delegate.createParameter(index);
    }

    /** @see buildcraft.api.statements.IStatement#rotateLeft() */
    @Override
    public IStatement rotateLeft() {
        return this.delegate.rotateLeft();
    }

    /** @see buildcraft.api.statements.IStatement#getGuiSprite() */
    @Override
    public TextureAtlasSprite getGuiSprite() {
        return this.delegate.getGuiSprite();
    }

    public TileEntity getNeighbourTile(IStatementContainer source) {
        return source.getNeighbourTile(sourcePart.face);
    }

    @Override
    public abstract StatementWrapper[] getPossible();

    @Override
    public int compareTo(StatementWrapper o) {
        if (sourcePart != o.sourcePart) {
            return Integer.compare(o.sourcePart.getIndex(), sourcePart.getIndex());
        }
        if (delegate == o.delegate) {
            return 0;
        }
        if (delegate.getClass() == o.delegate.getClass()) {
            IStatement[] poss = delegate.getPossible();
            IStatement[] oPoss = o.delegate.getPossible();
            if (Arrays.equals(poss, oPoss)) {
                int idxThis = -1;
                int idxThat = -1;
                for (int i = 0; i < poss.length; i++) {
                    if (poss[i] == delegate) {
                        idxThis = i;
                    }
                    if (poss[i] == o.delegate) {
                        idxThat = i;
                    }
                }
                if (idxThis != idxThat && idxThis != -1 && idxThat != -1) {
                    return Integer.compare(idxThis, idxThat);
                }
            }
        }
        return getUniqueTag().compareTo(o.getUniqueTag());
    }
}

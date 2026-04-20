/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package ct.buildcraft.api.filler;

import javax.annotation.Nullable;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.api.statements.IStatement;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.containers.IFillerStatementContainer;

/** A type of statement that is used for filler patterns. */
public interface IFillerPattern extends IStatement {
    /** @param filler The filler to create the pattern for.
     *            <br>
     *            NOTE: This method should never be called when {@link IFillerStatementContainer#hasBox()} returns
     *            false
     * @return The template to fill (should be created with
     * {@link IFillerRegistry#createFilledTemplate(BlockPos, BlockPos)}),
     * or {@code null} if this shouldn't make a template for the given filer. */
    @Nullable
    IFilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params);

    @Override
    IFillerPattern[] getPossible();

    /** Note that this sprite *must* be stitched to the texture atlas, as it is drawn on the side of the filler
     * block. */
    @Override
    ISprite getSprite();
}

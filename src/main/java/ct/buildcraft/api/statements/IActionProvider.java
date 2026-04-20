/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package ct.buildcraft.api.statements;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IActionProvider {
    void addInternalActions(Collection<IActionInternal> actions, IStatementContainer container);

    void addInternalSidedActions(Collection<IActionInternalSided> actions, IStatementContainer container, @Nonnull Direction side);

    void addExternalActions(Collection<IActionExternal> actions, @Nonnull Direction side, BlockEntity tile);
}

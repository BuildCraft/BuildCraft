/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package ct.buildcraft.api.statements;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

/** This is implemented by objects containing Statements, such as Gates and TileEntities. */
public interface IStatementContainer {
	BlockEntity getTile();

    @Nullable
    BlockEntity getNeighbourTile(Direction side);
}

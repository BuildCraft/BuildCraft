/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 */
package ct.buildcraft.api.boards;

public interface IRedstoneBoard<T> {
    void updateBoard(T container);

    RedstoneBoardNBT<?> getNBTHandler();
}

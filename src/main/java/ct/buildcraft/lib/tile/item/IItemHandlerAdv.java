/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.tile.item;

import net.minecraftforge.items.IItemHandler;

/** A form of {@link IItemHandler} that provides insertion-checking functionality via {@link StackInsertionChecker} */
public interface IItemHandlerAdv extends IItemHandler, StackInsertionChecker {}

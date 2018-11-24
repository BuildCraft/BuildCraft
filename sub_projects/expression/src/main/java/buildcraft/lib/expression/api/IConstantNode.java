/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.api;

import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.expression.node.value.NodeConstantObject;

/** Marker interface that means calling evaluate() on this will *always* return the same value.
 * <p>
 * This is provided for a single interface to check, however there should only ever be four classes that implement this:
 * {@link NodeConstantBoolean}, {@link NodeConstantLong}, {@link NodeConstantDouble}, and {@link NodeConstantObject}. */
public interface IConstantNode extends IExpressionNode {}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;

public interface ITickableNode {
    /** Called at any time */
    void refresh();

    /** Called once every minecraft tick. Used for variables that can changed depending on their previous value. */
    void tick();

    interface Source {
        ITickableNode createTickable();

        void setSource(IExpressionNode node);
    }
}

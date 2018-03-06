/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IVariableNode.IVariableNodeBoolean;

public class NodeVariableBoolean extends NodeVariable implements IVariableNodeBoolean {
    public boolean value;

    public NodeVariableBoolean(String name) {
        super(name);
    }

    @Override
    public boolean evaluate() {
        return value;
    }

    @Override
    public INodeBoolean inline() {
        if (isConst) {
            return NodeConstantBoolean.of(value);
        }
        return this;
    }

    @Override
    public void set(boolean value) {
        this.value = value;
    }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;


import buildcraft.lib.expression.node.value.ITickableNode;
import buildcraft.lib.expression.node.value.NodeStateful;
import buildcraft.lib.expression.node.value.NodeStateful.Instance;
import buildcraft.lib.expression.node.value.NodeUpdatable;

import java.util.Arrays;
import java.util.List;

public class ModelVariableData {
    private static int currentBakeId = 0;

    private int bakeId = -1;
    private ITickableNode[] tickableNodes;

    public static void onModelBake() {
        currentBakeId++;
    }

    public boolean hasNoNodes() {
        return tickableNodes == null;
    }

    public void setNodes(ITickableNode[] nodes) {
        bakeId = currentBakeId;
        tickableNodes = nodes;
    }

    public void addNodes(ITickableNode[] additional) {
        int originalLength = tickableNodes.length;
        tickableNodes = Arrays.copyOf(tickableNodes, originalLength + additional.length);
        System.arraycopy(additional, 0, tickableNodes, originalLength, additional.length);
    }

    private boolean checkModelBake() {
        if (tickableNodes == null) {
            return false;
        }
        if (currentBakeId == bakeId) {
            return true;
        }
        tickableNodes = null;
        return false;
    }

    /** @see ITickableNode#refresh() */
    public void refresh() {
        if (checkModelBake()) {
            for (ITickableNode node : tickableNodes) {
                node.refresh();
            }
        }
    }

    /** @see ITickableNode#tick() */
    public void tick() {
        if (checkModelBake()) {
            for (ITickableNode node : tickableNodes) {
                node.tick();
            }
        }
    }

    public void addDebugInfo(List<String> to) {
        if (tickableNodes != null) {
            for (ITickableNode node : tickableNodes) {
                if (node instanceof NodeUpdatable) {
                    NodeUpdatable nU = (NodeUpdatable) node;
                    to.add("  " + nU.name + " = " + nU.variable.evaluateAsString());
                } else if (node instanceof NodeStateful.Instance) {
                    NodeStateful.Instance nS = (Instance) node;
                    to.add("  " + nS.getContainer().name + " = " + nS.storedVar.evaluateAsString());
                }
            }
        }
    }
}

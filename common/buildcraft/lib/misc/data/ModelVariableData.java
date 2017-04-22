package buildcraft.lib.misc.data;

import java.util.List;

import buildcraft.lib.expression.node.value.ITickableNode;
import buildcraft.lib.expression.node.value.NodeStateful;
import buildcraft.lib.expression.node.value.NodeStateful.Instance;
import buildcraft.lib.expression.node.value.NodeUpdatable;

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
                    to.add("  " + nU.name + " = " + nU.variable.valueToString());
                } else if (node instanceof NodeStateful.Instance) {
                    NodeStateful.Instance nS = (Instance) node;
                    to.add("  " + nS.getContainer().name + " = " + nS.storedVar.valueToString());
                }
            }
        }
    }
}

package buildcraft.lib.expression.info;

import java.util.HashSet;
import java.util.Set;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;

/** An {@link IDependancyVisitor} that will check the properties of an {@link IDependantNode}. Specifically: */
public class DependencyVisitorCollector extends DependencyVisitorRouting {

    private boolean allConstant = true;
    private boolean needsUnkown = false;
    private final Set<IExpressionNode> mutableNodes;

    private DependencyVisitorCollector(Set<IExpressionNode> mutableNodes) {
        this.mutableNodes = mutableNodes;
    }

    public static DependencyVisitorCollector createConstantSearch() {
        return new DependencyVisitorCollector(null);
    }

    public static DependencyVisitorCollector createFullSearch() {
        return new DependencyVisitorCollector(new HashSet<>());
    }

    public static boolean testIsConstant(IDependantNode... node) {
        DependencyVisitorCollector search = createConstantSearch();
        search.dependOn(node);
        return search.areAllConstant();
    }

    public static Set<IExpressionNode> searchMutableNodes(IDependantNode... nodes) {
        DependencyVisitorCollector search = createFullSearch();
        search.dependOn(nodes);
        return search.getMutableNodes();
    }

    @Override
    protected boolean visit(IExpressionNode node) {
        if (node instanceof IConstantNode) {
            // No other properties can be true.
            return true;
        } else {
            allConstant = false;
            if (mutableNodes == null) {
                return false;
            }
            mutableNodes.add(node);
        }
        return true;
    }

    @Override
    public void dependOnUnknown() {
        needsUnkown = true;
    }

    /** @return True if all nodes passed to {@link #visit(IExpressionNode)} so far are {@link IConstantNode}'s. */
    public boolean areAllConstant() {
        return allConstant;
    }

    /** @return True if any of the nodes called {@link #dependOnUnknown()}. */
    public boolean needsUnkown() {
        return needsUnkown;
    }

    /** @return All mutable nodes encountered by this search.
     * @throws IllegalStateException if this was constructed by {@link #createConstantSearch()} (as then this returned
     *             set will be null, or inaccurate). */
    public Set<IExpressionNode> getMutableNodes() {
        if (mutableNodes == null) {
            throw new IllegalStateException(
                "Attempted to get a list of all mutable nodes when this object was constructed from #createConstantSearch()!");
        }
        return mutableNodes;
    }
}

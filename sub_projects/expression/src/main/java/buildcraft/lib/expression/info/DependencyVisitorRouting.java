package buildcraft.lib.expression.info;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;

public abstract class DependencyVisitorRouting implements IDependancyVisitor {
    private boolean openForVisiting = true;

    /** Resets the {@link #isOpenForVisiting()} property of this visitor. */
    protected void reopen() {
        openForVisiting = true;
    }

    protected void close() {
        openForVisiting = false;
    }

    public boolean isOpenForVisiting() {
        return openForVisiting;
    }

    /** @return False if this visitor has collected all of the information it can possibly collect from the parent. For
     *         example if all the subclass cares about is if a given node only depends on {@link IConstantNode}'s, then
     *         this will return false after the first non-constant node. */
    protected abstract boolean visit(IExpressionNode node);

    private boolean visitPotentialDependantNode(IExpressionNode node) {
        if (node instanceof IDependantNode) {
            dependOn((IDependantNode) node);
            return isOpenForVisiting();
        } else {
            return visit(node);
        }
    }

    @Override
    public void dependOnExplictly(IExpressionNode node) {
        visit(node);
    }

    @Override
    public void dependOn(IExpressionNode node) {
        if (!isOpenForVisiting()) {
            return;
        }
        visitPotentialDependantNode(node);
    }

    @Override
    public void dependOn(IExpressionNode... nodes) {
        if (isOpenForVisiting()) {
            for (IExpressionNode node : nodes) {
                if (!visitPotentialDependantNode(node)) {
                    close();
                    return;
                }
            }
        }
    }

    @Override
    public void dependOnNodes(Iterable<? extends IExpressionNode> nodes) {
        if (isOpenForVisiting()) {
            for (IExpressionNode node : nodes) {
                if (!visitPotentialDependantNode(node)) {
                    close();
                    return;
                }
            }
        }
    }

    private boolean visit(IDependantNode child) {
        child.visitDependants(this);
        return isOpenForVisiting();
    }

    @Override
    public void dependOn(IDependantNode child) {
        visit(child);
    }

    @Override
    public void dependOn(IDependantNode... children) {
        if (isOpenForVisiting()) {
            for (IDependantNode child : children) {
                if (!visit(child)) {
                    return;
                }
            }
        }
    }

    @Override
    public void dependOnChildren(Iterable<? extends IDependantNode> children) {
        if (isOpenForVisiting()) {
            for (IDependantNode child : children) {
                if (!visit(child)) {
                    return;
                }
            }
        }
    }
}

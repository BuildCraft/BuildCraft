package ct.buildcraft.lib.expression.api;

/** An object that depends on some nodes. */
public interface IDependantNode {
    void visitDependants(IDependancyVisitor visitor);
}

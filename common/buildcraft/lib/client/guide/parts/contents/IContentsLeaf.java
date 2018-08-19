package buildcraft.lib.client.guide.parts.contents;

public interface IContentsLeaf extends IContentsNode {
    @Override
    default void calcVisibility() {
        // NO-OP
    }

    @Override
    default void sort() {
        // NO-OP
    }

    @Override
    default void addChild(IContentsNode node) {
        // NO-OP
    }

    @Override
    default IContentsNode[] getVisibleChildren() {
        return new IContentsNode[0];
    }
}

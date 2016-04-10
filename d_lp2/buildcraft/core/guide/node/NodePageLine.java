package buildcraft.core.guide.node;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import buildcraft.core.guide.PageLine;

public class NodePageLine implements Comparable<NodePageLine> {
    public final NodePageLine parent;
    public final PageLine pageLine;
    private final List<NodePageLine> children = Lists.newArrayList();
    public boolean expanded = true;

    public NodePageLine(NodePageLine parent, PageLine pageLine) {
        this.parent = parent;
        this.pageLine = pageLine;
    }

    public NodePageLine addChild(PageLine line) {
        NodePageLine node = new NodePageLine(this, line);
        children.add(node);
        return node;
    }

    public Iterable<NodePageLine> iterateNonNullNodes() {
        return new Iterable<NodePageLine>() {
            @Override
            public Iterator<NodePageLine> iterator() {
                return new NodeIterator(false);
            }
        };
    }

    public Iterable<NodePageLine> iterateOnlyExpandedNodes() {
        return new Iterable<NodePageLine>() {
            @Override
            public Iterator<NodePageLine> iterator() {
                return new NodeIterator(true);
            }
        };
    }

    public Iterable<PageLine> iterateNonNullLines() {
        return new Iterable<PageLine>() {
            @Override
            public Iterator<PageLine> iterator() {
                return new NodePageLineIterator(false);
            }
        };
    }

    public Iterable<PageLine> iterateOnlyExpandedLines() {
        return new Iterable<PageLine>() {
            @Override
            public Iterator<PageLine> iterator() {
                return new NodePageLineIterator(true);
            }
        };
    }

    public List<NodePageLine> getChildren() {
        return Collections.unmodifiableList(children);
    }

    private List<NodePageLine> getChildren(boolean skipIfNonExpanded) {
        if (expanded || !skipIfNonExpanded) {
            return children;
        }
        return Collections.emptyList();
    }

    public NodePageLine getChildNode(PageLine line) {
        for (NodePageLine node : iterateNonNullNodes()) {
            if (node.pageLine == line) {
                return node;
            }
        }
        return null;
    }

    public void sortChildrenRecursivly() {
        Collections.sort(children);
        for (NodePageLine child : children) {
            child.sortChildrenRecursivly();
        }
    }

    @Override
    public int compareTo(NodePageLine o) {
        return pageLine.compareTo(o.pageLine);
    }

    private class NodeIterator implements Iterator<NodePageLine> {
        private final boolean skipNonExpanded;
        private NodePageLine current;
        private int childrenDone = 0;

        NodeIterator(boolean skipNonExpanded) {
            this.skipNonExpanded = skipNonExpanded;
            current = NodePageLine.this;
        }

        @Override
        public boolean hasNext() {
            return next(true) != null;
        }

        @Override
        public NodePageLine next() {
            return next(false);
        }

        private NodePageLine next(boolean simulate) {
            NodePageLine current = this.current;
            int childrenDone = this.childrenDone;
            while (childrenDone == current.getChildren(skipNonExpanded).size()) {
                // Go to the parent
                NodePageLine child = current;
                current = current.parent;
                if (current == null) {
                    return null;
                }
                childrenDone = current.getChildren(skipNonExpanded).indexOf(child) + 1;
            }
            NodePageLine parent = current;
            current = parent.getChildren(skipNonExpanded).get(childrenDone++);
            childrenDone = 0;
            if (!simulate) {
                this.current = current;
                this.childrenDone = childrenDone;
            }
            return current;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

    private class NodePageLineIterator implements Iterator<PageLine> {
        private final NodeIterator iterator;

        private NodePageLineIterator(boolean skipNonExpanded) {
            iterator = new NodeIterator(skipNonExpanded);
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public PageLine next() {
            return iterator.next().pageLine;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}

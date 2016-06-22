package buildcraft.lib.client.guide.node;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuideText;

public class NodePageLine implements Comparable<NodePageLine> {
    public final NodePageLine parent;
    public final GuidePart part;
    private final List<NodePageLine> children = Lists.newArrayList();

    public NodePageLine(NodePageLine parent, GuidePart part) {
        this.parent = parent;
        this.part = part;
    }

    public NodePageLine addChild(GuidePart part) {
        NodePageLine node = new NodePageLine(this, part);
        children.add(node);
        return node;
    }

    public void setFontRenderer(IFontRenderer fontRenderer) {
        if (part != null) {
            part.setFontRenderer(fontRenderer);
        }
        for (NodePageLine node : children) {
            node.setFontRenderer(fontRenderer);
        }
    }

    public Iterable<NodePageLine> iterateNonNullNodes() {
        return new Iterable<NodePageLine>() {
            @Override
            public Iterator<NodePageLine> iterator() {
                return new NodePartIterator();
            }
        };
    }

    public Iterable<GuidePart> iterateNonNullLines() {
        return new Iterable<GuidePart>() {
            @Override
            public Iterator<GuidePart> iterator() {
                return new NodeGuidePartIterator();
            }
        };
    }

    public List<NodePageLine> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public NodePageLine getChildNode(GuidePart line) {
        for (NodePageLine node : iterateNonNullNodes()) {
            if (node.part == line) {
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

    private String getString() {
        if (part instanceof GuideText) {
            return ((GuideText) part).text.text;
        } else if (part instanceof GuideChapter) {
            return ((GuideChapter) part).chapter.text;
        } else {
            return part == null ? "null" : part.toString();
        }
    }

    @Override
    public int compareTo(NodePageLine o) {
        return getString().compareTo(o.getString());
    }

    private class NodePartIterator implements Iterator<NodePageLine> {
        private NodePageLine current;
        private int childrenDone = 0;

        NodePartIterator() {
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
            while (childrenDone == current.getChildren().size()) {
                // Go to the parent
                NodePageLine child = current;
                current = current.parent;
                if (current == null) {
                    return null;
                }
                childrenDone = current.getChildren().indexOf(child) + 1;
            }
            NodePageLine parent = current;
            current = parent.getChildren().get(childrenDone++);
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

    private class NodeGuidePartIterator implements Iterator<GuidePart> {
        private final NodePartIterator iterator;

        private NodeGuidePartIterator() {
            iterator = new NodePartIterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public GuidePart next() {
            return iterator.next().part;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}

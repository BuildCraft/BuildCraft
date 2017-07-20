/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.node;

import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuideText;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NodePageLine implements Comparable<NodePageLine> {
    public final NodePageLine parent;
    public final GuidePart part;
    private final List<NodePageLine> children = Lists.newArrayList();

    public NodePageLine(NodePageLine parent, GuidePart part) {
        this.parent = parent;
        this.part = part;
    }

    public NodePageLine addChild(GuidePart child) {
        NodePageLine node = new NodePageLine(this, child);
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
        return NodePartIterator::new;
    }

    public Iterable<GuidePart> iterateNonNullLines() {
        return NodeGuidePartIterator::new;
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

    public void sortChildrenRecursively() {
        Collections.sort(children);
        for (NodePageLine child : children) {
            child.sortChildrenRecursively();
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
            NodePageLine next = this.current;
            int visited = this.childrenDone;
            while (visited == next.getChildren().size()) {
                // Go to the parent
                NodePageLine child = next;
                next = next.parent;
                if (next == null) {
                    return null;
                }
                visited = next.getChildren().indexOf(child) + 1;
            }
            next = next.getChildren().get(visited++);
            visited = 0;
            if (!simulate) {
                this.current = next;
                this.childrenDone = visited;
            }
            return next;
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

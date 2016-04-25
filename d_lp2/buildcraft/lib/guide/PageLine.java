package buildcraft.lib.guide;

import buildcraft.lib.gui.GuiIcon;

/** Stores information about a single line of text. This may be displayed as more than a single line though. */
public class PageLine implements Comparable<PageLine> {
    /** Can be any of the boxes, any icon with dimensions different to these will render incorrectly. */
    public GuiIcon startIcon;
    public GuiIcon startIconHovered;
    public final int indent;
    /** This will be wrapped automatically when it is rendered. */
    public final String text;
    public final boolean link;

    public PageLine(int indent, String text, boolean isLink) {
        this(null, null, indent, text, isLink);
    }

    public PageLine(GuiIcon startIcon, GuiIcon startIconHovered, int indent, String text, boolean isLink) {
        this.startIcon = startIcon;
        this.startIconHovered = startIconHovered;
        this.indent = indent;
        this.text = text;
        this.link = isLink;
    }

    @Override
    public String toString() {
        return "PageLine [indent = " + indent + ", text=" + text + "]";
    }

    @Override
    public int compareTo(PageLine o) {
        return text.toLowerCase().compareTo(o.text.toLowerCase());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + indent;
        result = prime * result + (link ? 1231 : 1237);
        result = prime * result + ((startIcon == null) ? 0 : startIcon.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        PageLine other = (PageLine) obj;
        if (indent != other.indent) return false;
        if (link != other.link) return false;
        if (startIcon == null) {
            if (other.startIcon != null) return false;
        } else if (!startIcon.equals(other.startIcon)) return false;
        if (text == null) {
            if (other.text != null) return false;
        } else if (!text.equals(other.text)) return false;
        return true;
    }
}

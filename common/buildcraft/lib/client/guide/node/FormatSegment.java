package buildcraft.lib.client.guide.node;

import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.util.Formatting;

/** A segment of a larger text */
public final class FormatSegment {
    public final String text;
    public final Formatting colour;
    public final Set<Formatting> misc;

    FormatSegment(String text, Formatting colour, Set<Formatting> misc) {
        this.text = text;
        this.colour = colour;
        this.misc = misc;
    }

    @Nullable
    public FormatSegment join(FormatSegment other) {
        if (colour == other.colour && misc.equals(other.misc)) {
            return new FormatSegment(text + other.text, colour, misc);
        }
        return null;
    }

    public String toFormatString() {
        StringBuilder miscString = new StringBuilder();
        for (Formatting format : misc) {
            miscString.append(format.toString());
        }
        return Formatting.RESET + (colour == null ? "" : colour.toString()) + miscString + text;
    }

    @Override
    public String toString() {
        StringBuilder miscStr = new StringBuilder();
        for (Formatting format : misc) {
            miscStr.append(format.getFriendlyName());
            miscStr.append(' ');
        }
        return (colour == null ? "" : (colour.getFriendlyName() + ""))//
            + miscStr + text;
    }
}

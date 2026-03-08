package buildcraft.lib.client.guide.node;


import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Set;

/** A segment of a larger text */
public final class FormatSegment {
    public final String text;
    public final TextFormatting colour;
    public final Set<TextFormatting> misc;

    FormatSegment(String text, TextFormatting colour, Set<TextFormatting> misc) {
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
        for (TextFormatting format : misc) {
            miscString.append(format.toString());
        }
        return TextFormatting.RESET + (colour == null ? "" : colour.toString()) + miscString + text;
    }

    @Override
    public String toString() {
        StringBuilder miscStr = new StringBuilder();
        for (TextFormatting format : misc) {
            miscStr.append(format.getName());
            miscStr.append(' ');
        }
        return (colour == null ? "" : (colour.getName() + ""))//
                + miscStr + text;
    }
}

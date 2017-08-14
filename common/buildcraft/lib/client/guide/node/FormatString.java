package buildcraft.lib.client.guide.node;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

import net.minecraft.util.text.TextFormatting;

import buildcraft.lib.client.guide.font.IFontRenderer;

public class FormatString {
    private static final String WORD_GAP = " \n\t";
    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)\u00a7[0-9A-FK-OR]");

    public final FormatSegment[] segments;

    public FormatString(FormatSegment[] segments) {
        this.segments = segments;
    }

    public static FormatString split(String formattedText) {
        List<FormatSegment> segments = new ArrayList<>();

        TextFormatting lastColour = null;
        Set<TextFormatting> lastMisc = EnumSet.noneOf(TextFormatting.class);
        int lastEnd = 0;

        Matcher matcher = FORMATTING_CODE_PATTERN.matcher(formattedText);
        while (matcher.find()) {
            int start = matcher.start();

            if (start != lastEnd) {
                String subText = formattedText.substring(lastEnd, start);
                ImmutableSet<TextFormatting> miscCopy = ImmutableSet.copyOf(lastMisc);
                segments.add(new FormatSegment(subText, lastColour, miscCopy));
            }

            String matched = matcher.group();
            TextFormatting format = null;
            for (TextFormatting f : TextFormatting.values()) {
                if (f.toString().equals(matched)) {
                    format = f;
                    break;
                }
            }
            if (format != null) {
                if (format == TextFormatting.RESET) {
                    lastColour = null;
                    lastMisc.clear();
                } else if (format.isColor()) {
                    lastColour = format;
                } else {
                    lastMisc.add(format);
                }
            }
            lastEnd = matcher.end();
        }
        if (lastEnd != formattedText.length()) {
            String subText = formattedText.substring(lastEnd);
            ImmutableSet<TextFormatting> miscCopy = ImmutableSet.copyOf(lastMisc);
            segments.add(new FormatSegment(subText, lastColour, miscCopy));
        }
        return new FormatString(segments.toArray(new FormatSegment[0]));
    }

    /** @return An array of length 1 or 2, with this wrapped down onto the next line. Note that this only wraps a single
     *         line. */
    public FormatString[] wrap(IFontRenderer font, int maxWidth) {
        return wrap(font, maxWidth, true);
    }

    /** @return An array of length 1 or 2, with this wrapped down onto the next line. Note that this only wraps a single
     *         line. */
    public FormatString[] wrap(IFontRenderer font, int maxWidth, boolean onWords) {
        List<FormatSegment> thisLine = new ArrayList<>();
        int widthUsed = 0;

        for (FormatSegment segment : segments) {
            String wholeText = segment.text;
            // Join words, if appropriate
            
            // TODO: Ensure that this segment doesn't join with the NEXT segment as a word!
            int width = font.getStringWidth(segment.text);
            if (width + widthUsed < maxWidth) {
                thisLine.add(segment);
                widthUsed += width;
            } else {
                String text = segment.text;
                for (int i = text.length(); i > 0; i--) {
                    String c = text.substring(i, i + 1);
                    if (onWords && !WORD_GAP.contains(c)) {
                        continue;
                    }
                }
            }
        }

        return new FormatString[] { this };

        for (int i = text.length(); i > 0; i--) {
            String c = text.substring(i, i + 1);
            if (onWords && !WORD_GAP.contains(c)) {
                continue;
            }
            String start = text.substring(0, i);
            int w = font.getStringWidth(start);
            if (w <= width) {
                if (i == text.length()) {
                    return new FormatSegment[] { this };
                } else {
                    return new FormatSegment[] { //
                        new FormatSegment(start, colour, misc), //
                        new FormatSegment(text.substring(i), colour, misc)//
                    };
                }
            }
        }
        return new FormatSegment[] { //
            new FormatSegment(text.substring(0, 1), colour, misc), //
            new FormatSegment(text.substring(1), colour, misc)//
        };
    }
}

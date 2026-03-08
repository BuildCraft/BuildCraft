package buildcraft.lib.client.guide.node;

import buildcraft.lib.client.guide.font.IFontRenderer;
import com.google.common.collect.ImmutableSet;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatString {
    private static final String WORD_GAP = " \n\t";
    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)\u00a7[0-9A-FK-OR]");

    public final FormatSegment[] segments;
    private final String unformatted, formatted;

    public FormatString(FormatSegment[] segments) {
        this.segments = segments;
        String s = "", sf = "";
        for (FormatSegment seg : segments) {
            s += seg.text;
            sf += seg.toFormatString();
        }
        unformatted = s;
        formatted = sf;
    }

    public String getFormatted() {
        return formatted;
    }

    public String getUnformatted() {
        return unformatted;
    }

    public static FormatString split(String formattedText) {
        List<FormatSegment> segments = new ArrayList<>();

        ChatFormatting lastColour = null;
        Set<ChatFormatting> lastMisc = EnumSet.noneOf(ChatFormatting.class);
        int lastEnd = 0;

        Matcher matcher = FORMATTING_CODE_PATTERN.matcher(formattedText);
        while (matcher.find()) {
            int start = matcher.start();

            if (start != lastEnd) {
                String subText = formattedText.substring(lastEnd, start);
                ImmutableSet<ChatFormatting> miscCopy = ImmutableSet.copyOf(lastMisc);
                segments.add(new FormatSegment(subText, lastColour, miscCopy));
            }

            String matched = matcher.group();
            ChatFormatting format = null;
            for (ChatFormatting f : ChatFormatting.values()) {
                if (f.toString().equals(matched)) {
                    format = f;
                    break;
                }
            }
            if (format != null) {
                if (format == ChatFormatting.RESET) {
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
            ImmutableSet<ChatFormatting> miscCopy = ImmutableSet.copyOf(lastMisc);
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

        for (int segmentIndex = 0; segmentIndex < segments.length; segmentIndex++) {
            FormatSegment segment = segments[segmentIndex];

            // TODO: Ensure that this segment doesn't join with the NEXT segment as a word!
            int width = font.getStringWidth(segment.toFormatString());
            if (width + widthUsed <= maxWidth) {
                thisLine.add(segment);
                widthUsed += width;
            } else {
                String text = segment.toFormatString();
                int allowedLength = 1;
                boolean words = onWords;
                outer:
                while (true) {
                    for (int i = text.length(); i > 1; i--) {
                        String c = text.substring(i - 1, i);
                        if (words && !WORD_GAP.contains(c)) {
                            continue;
                        }
                        String subText = text.substring(0, i);
                        int w = font.getStringWidth(subText);
                        if (w + widthUsed <= maxWidth) {
                            allowedLength = i;
                            break outer;
                        }
                    }
                    if (words && segmentIndex == 0) {
                        words = false;
                    } else {
                        break;
                    }
                }
                int i = allowedLength;
                if (i > 1) {
                    String subText = text.substring(0, allowedLength);
                    int left = segments.length - segmentIndex;
                    FormatSegment[] next = new FormatSegment[left];
                    thisLine.add(new FormatSegment(subText, segment.colour, segment.misc));
                    next[0] = new FormatSegment(text.substring(i), segment.colour, segment.misc);
                    for (int j = 1; j < left; j++) {
                        next[j] = segments[segmentIndex + j];
                    }
                    return new FormatString[] { //
                            new FormatString(thisLine.toArray(new FormatSegment[0])), //
                            new FormatString(next)//
                    };
                } else {
                    int left = segments.length - segmentIndex;
                    FormatSegment[] next = new FormatSegment[left];
                    for (int j = 0; j < left; j++) {
                        next[j] = segments[j + 1];
                    }
                    return new FormatString[] { //
                            new FormatString(thisLine.toArray(new FormatSegment[0])), //
                            new FormatString(next)//
                    };
                }
            }
        }
        return new FormatString[] { this };
    }
}

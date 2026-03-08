package buildcraft.lib.script;

import com.google.common.collect.ImmutableList;

public class LineData {
    public final String text;
    public final String lineNumbers;
    public final SourceLine original;
    /** All sources. Doesn't include the original. */
    public final ImmutableList<SourceLine> firstLineSources;

    public LineData(String text, SourceFile file, int line) {
        this.text = text;
        this.original = new SourceLine(file, line);
        this.firstLineSources = ImmutableList.of();
        this.lineNumbers = line + 1 + "";
    }

    public LineData(LineData from, String text) {
        this.text = text;
        this.lineNumbers = from.lineNumbers;
        this.original = from.original;
        this.firstLineSources = from.firstLineSources;
    }

    private LineData(String text, LineData original, int nextLine, ImmutableList<SourceLine> sources) {
        this.text = text;
        this.original = original.original;
        this.firstLineSources = sources;
        this.lineNumbers = original.lineNumbers + "(" + (nextLine + 1) + ")";
    }

    public LineData createReplacement(String newText, SourceLine newSource, int newLine) {
        ImmutableList<SourceLine> list =
                ImmutableList.<SourceLine>builder().addAll(firstLineSources).add(newSource).build();
        return new LineData(newText, this, newLine, list);
    }

    @Override
    public String toString() {
        return original + " " + lineNumbers + ": " + firstLineSources + " " + text;
    }
}

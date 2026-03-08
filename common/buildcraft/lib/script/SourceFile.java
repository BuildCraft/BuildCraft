package buildcraft.lib.script;

public final class SourceFile {
    public final String name;
    public final int lineCount;

    public SourceFile(String name, int lineCount) {
        this.name = name;
        this.lineCount = lineCount;
    }
}

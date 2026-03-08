package buildcraft.lib.script;

import java.util.Objects;

public final class SourceLine {
    public final SourceFile file;
    public final int line;

    public SourceLine(SourceFile file, int line) {
        this.file = file;
        this.line = line;
    }

    public void appendLineNumber(StringBuilder sb) {
        // Actually we don't do this do we?
        sb.append(line);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, line);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        SourceLine other = (SourceLine) obj;
        return line == other.line && file.equals(other.file);
    }

    @Override
    public String toString() {
        return file.name + "." + (line + 1);
    }
}

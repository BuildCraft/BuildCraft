package buildcraft.lib.script;

import javax.annotation.Nullable;

public class ScriptAliasFunction {
    public static class AliasBuilder {
        public String name;
        public LineData[] rawOutputs;
        public int startLine;
        public int argCount;
        public ScriptAliasDocumentation docs;
    }

    public final String name;
    public final LineData[] rawOutput;
    public final int startLine;
    public final int argCount;

    @Nullable
    public final ScriptAliasDocumentation docs;

    public ScriptAliasFunction(AliasBuilder builder) {
        this.name = builder.name;
        this.rawOutput = builder.rawOutputs;
        this.startLine = builder.startLine;
        this.argCount = builder.argCount;
        this.docs = builder.docs;
    }
}

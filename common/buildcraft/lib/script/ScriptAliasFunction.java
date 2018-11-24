package buildcraft.lib.script;

import java.util.function.Function;

import javax.annotation.Nullable;

import buildcraft.lib.script.SimpleScript.LineData;

public class ScriptAliasFunction {
    @Deprecated
    public enum ArgType {
        STRING(SimpleScript::nextQuotedArg),
        NUMBER(SimpleScript::nextSimpleArg);

        @Deprecated
        private final Function<SimpleScript, String> getter;

        private ArgType(Function<SimpleScript, String> getter) {
            this.getter = getter;
        }

        @Deprecated
        @Nullable
        public final String next(SimpleScript script) {
            return getter.apply(script);
        }
    }

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

package buildcraft.lib.script;

import com.google.gson.JsonSyntaxException;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/** Provides logging and error feedback for a single script file. */
public interface IScriptFileLog {
    /* Notes: This might be used in the future for a GUI based INSN viewer/editor to make it simple to switch from
     * logging to a file and showing the error in the UI. However until it's actually implemented this should stay
     * unused. */

    static final int END_OF_LINE = Integer.MAX_VALUE;

    void error(int line, int startIndex, int endIndex, String message);

    default void error(int line, String message) {
        error(line, 0, END_OF_LINE, message);
    }

    // General

    /** Called at the very start
     *
     * @param file The file that is being loaded, or null if this is the main file.
     * @param lines The raw, original, contents of the file. */
    void populateFile(@Nullable SourceFile file, List<String> lines);

    /** Called when any function is missing a mandatory argument. If a method requires 6 arguments and only 2 are given
     * then this will be called 4 times.
     *
     * @param argDesc The documentation given for the argument index. */
    void errorMissingArgument(int line, int argIndex, String argDesc);

    /** Called when we skip over an if block. */
    void infoSkippingIfBlock(int line);

    /** Called when we stop skipping over an if block. This may be called multiple times, and doesn't necessarily
     * indicate that the next line will be executed. */
    void infoEndSkipping(int line);

    void infoConditionalResult(int tokenStart, int startIndex, int endIndex, boolean shouldCall);

    // Standard functions

    /** Called when we encounter an unknown function call. */
    void errorFunctionUnknown(int line, int startIndex, int endIndex, Collection<String> knownFunctions);

    /** Called when a standard function (add, modify, overwrite, remove, replace) call is missing it's name. */
    default void errorStdMissingName(int line) {
        error(line, "Missing name: ");
    }

    /** Called when a standard function (add, modify, overwrite, replace) has invalid json. */
    void errorStdInvalidJson(int line, JsonSyntaxException jse);

    /** Called when a standard function (add, modify, overwrite, remove, replace) statement has linked to a file that
     * doesn't exist. */
    void errorStdUnknownFile(int line, String file);

    // Imports

    /** Called if an import statement is missing the (mandatory) file argument. */
    default void errorImportMissingFile(int line) {
        error(line, "Cannot find the file ");
    }

    /** Called if a valid file argument was found, but the file itself cannot be found. */
    void errorImportNotFound(int line, String sourceFile);

    /** Called when an import file is missing the standard "~{buildcraft/json/lib}" on the very first line. */
    void errorImportMissingStarter(int line, String sourceFile);

    /** Called when an already imported or aliased statement tries to re-replace an existing replacement over itself. */
    void errorImportRecursiveReplace(int line, String newSourceFile);

    // Aliasing

    /** Called when aliased function is defined with an invalid number of arguments. (For example -1 or "hello" has been
     * passed as the argument number). */
    void errorAliasInvalidArgCount(int line, int startIndex, int endIndex, @Nullable Integer parsed);

    default void errorAliasMissingName(int tokenStart) {
        errorMissingArgument(tokenStart, 0, "The custom name for the function");
    }

    default void errorAliasMissingArgCount(int tokenStart) {
        errorMissingArgument(tokenStart, 1, "The number of arguments for the function");
    }

    default void errorAliasMissingReplacement(int line) {
        errorMissingArgument(line, 2,
                "The replacement for the alias. This can include ${1} and ${2} etc for the aliased arguments.");
    }

    // Replacement

    /** @param removeStart The first line to remove, from the original (unreplaced) file.
     * @param removeEnd The last line to remove, from the original (unreplaced) file.
     * @param from The file to take the replacements from, or null if it is the same file.
     * @param fromStart
     * @param newLines */
    void replace(int removeStart, int removeEnd, @Nullable SourceFile from, int fromStart, List<String> newLines);
}

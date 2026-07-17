package buildcraft.lib.script;

import java.util.Collection;
import java.util.List;

import com.google.gson.JsonSyntaxException;

import buildcraft.api.core.BCLog;

public class SimpleScriptFileLog implements IScriptFileLog {

    private static void log(String s) {
        BCLog.logger.info("[lib.script]   " + s);
    }

    private static void log(int l, String s) {
        log(l + ": " + s);
    }

    private static void log(SourceLine l, String s) {
        log(l.line, s);
    }

    @Override
    public void populateFile(SourceFile file, List<String> lines) {
        log("File: " + file);
        int i = 1;
        for (String line : lines) {
            log(i + ": " + line);
            i++;
        }
    }

    @Override
    public void error(int line, int startIndex, int endIndex, String message) {
        log(line, message);
    }

    @Override
    public void errorMissingArgument(int line, int argIndex, String argDesc) {
        log(line, "Missing argument #" + argIndex + " (" + argDesc + ")");
    }

    @Override
    public void infoSkippingIfBlock(int line) {
        log(line, "Skipping if block");
    }

    @Override
    public void infoEndSkipping(int line) {
        log(line, "End of skipped block");
    }

    @Override
    public void infoConditionalResult(int tokenStart, int startIndex, int endIndex, boolean shouldCall) {
        log(tokenStart, "Conditional evaluated to " + shouldCall + " (range " + startIndex + "-" + endIndex + ")");
    }

    @Override
    public void errorFunctionUnknown(int line, int startIndex, int endIndex, Collection<String> knownFunctions) {
        log(line, "Unknown function (range " + startIndex + "-" + endIndex + "). Known: " + knownFunctions);
    }

    @Override
    public void errorStdInvalidJson(int line, JsonSyntaxException jse) {
        log(line, "Invalid JSON: " + jse.getMessage());
    }

    @Override
    public void errorStdUnknownFile(int line, String file) {
        log(line, "Unknown file referenced: " + file);
    }

    @Override
    public void errorImportNotFound(int line, String sourceFile) {
        log(line, "Import not found: " + sourceFile);
    }

    @Override
    public void errorImportMissingStarter(int line, String sourceFile) {
        log(line, "Import missing starter line: " + sourceFile);
    }

    @Override
    public void errorImportRecursiveReplace(int line, String newSourceFile) {
        log(line, "Recursive import replacement detected: " + newSourceFile);
    }

    @Override
    public void errorAliasInvalidArgCount(int line, int startIndex, int endIndex, Integer parsed) {
        log(line, "Invalid alias argument count " + parsed + " (range " + startIndex + "-" + endIndex + ")");
    }

    @Override
    public void replace(int removeStart, int removeEnd, SourceFile from, int fromStart, List<String> newLines) {
        String src = from == null ? "this file" : from.toString();
        log("Replacing lines " + removeStart + "-" + removeEnd + " from " + src + " starting at " + fromStart);
    }
}

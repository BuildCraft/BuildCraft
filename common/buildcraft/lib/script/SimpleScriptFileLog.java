package buildcraft.lib.script;

import buildcraft.api.core.BCLog;
import com.google.gson.JsonSyntaxException;

import java.util.Collection;
import java.util.List;

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
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public void infoEndSkipping(int line) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public void infoConditionalResult(int tokenStart, int startIndex, int endIndex, boolean shouldCall) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public void errorFunctionUnknown(int line, int startIndex, int endIndex, Collection<String> knownFunctions) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public void errorStdInvalidJson(int line, JsonSyntaxException jse) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public void errorStdUnknownFile(int line, String file) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public void errorImportNotFound(int line, String sourceFile) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public void errorImportMissingStarter(int line, String sourceFile) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public void errorImportRecursiveReplace(int line, String newSourceFile) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public void errorAliasInvalidArgCount(int line, int startIndex, int endIndex, Integer parsed) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public void replace(int removeStart, int removeEnd, SourceFile from, int fromStart, List<String> newLines) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }
}

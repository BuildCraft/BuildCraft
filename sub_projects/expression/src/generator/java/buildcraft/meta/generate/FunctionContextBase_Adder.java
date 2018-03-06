package buildcraft.meta.generate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionContextBase_Adder extends AutoGenerateFile {

    static final List<Map<String, String>> allFunctionTypes = new ArrayList<>();

    private static final String PATH = "/buildcraft/lib/expression/FunctionContextBase.java";

    public FunctionContextBase_Adder() {
        super(PATH + ".txt");
    }

    @Override
    protected void callFileGen() {
        String replace = "";
        String imports = "";

        for (Map<String, String> types : allFunctionTypes) {
            // Base method
            String method = "\n\tpublic {$TypeArgs} NodeFunc{$Args}To{$ReturnOnly}{$TypeArgs} put_{$a}_{$r}("
                + "String name, {$ObjectClassArgs}IFunc{$Args}To{$ReturnOnly}{$TypeArgs} func) {";
            method += "\n\t\treturn putFunction(name, new NodeFunc{$Args}To{$ReturnOnly}";
            method.replace("  ", " ");
            if (types.get("ObjectClassArgs").length() > 0) {
                method += "<>";
            }
            method += "(name, {$ObjectClassArgsPass}func));";
            method += "\n\t}\n";

            // Variant with a string function
            method += "\n\tpublic {$TypeArgs} NodeFunc{$Args}To{$ReturnOnly}{$TypeArgs} put_{$a}_{$r}("
                + "String name, {$ObjectClassArgs}IFunc{$Args}To{$ReturnOnly}{$TypeArgs} func, {$StringFunction} stringFunction) {";
            method += "\n\t\treturn putFunction(name, new NodeFunc{$Args}To{$ReturnOnly}";
            method.replace("  ", " ");
            if (types.get("ObjectClassArgs").length() > 0) {
                method += "<>";
            }
            method += "({$ObjectClassArgsPass}func, stringFunction));";
            method += "\n\t}\n";

            // Imports
            method = replaceAll(method, types);

            replace += method;
            String importStart = "import buildcraft.lib.expression.node.func.NodeFunc";
            imports += importStart + replaceAll("{$Args}To{$ReturnOnly};\n", types);
            imports += importStart + replaceAll("{$Args}To{$ReturnOnly}.IFunc{$Args}To{$ReturnOnly};\n", types);
        }

        Map<String, String> map = new HashMap<>();
        map.put("PutMethods", replace);
        map.put("Imports", imports);
        generateFile(PATH, map);
    }
}

package buildcraft.meta.generate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public abstract class AutoGenerateFile {
    public static final String AUTO_GEN_WARNING = "// AUTO_GENERATED FILE, DO NOT EDIT MANUALLY!";
    public final String input;

    private File outputFileBase;
    private String inputFileContents;

    public AutoGenerateFile(String input) {
        this.input = input;
    }

    protected abstract void callFileGen();

    public final void generateFiles(File outputBase) {
        outputFileBase = outputBase;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(input)))) {
            StringBuilder builder = new StringBuilder();
            String s;
            while ((s = reader.readLine()) != null) {
                builder.append(s);
                builder.append(System.lineSeparator());
            }
            inputFileContents = builder.toString();
        } catch (IOException e) {
            throw new Error(e);
        }
        callFileGen();
    }

    protected final void generateFile(String outputFile, Map<String, String> replacements) {
        String realOutput = inputFileContents;

        realOutput = replaceAll(realOutput, replacements);
        outputFile = replaceAll(outputFile, replacements);
        realOutput = realOutput.replace("{$AutoGenWarning}", AUTO_GEN_WARNING);
        realOutput = realOutput.replace("\t", "    ");
        if (outputFileBase != null) {
            File out = new File(outputFileBase, outputFile);
            out.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(out)) {
                writer.write(realOutput);
                writer.flush();
            } catch (IOException e) {
                throw new Error(e);
            }
        }
    }

    public static String replaceAll(String src, Map<String, String> replacements) {
        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            String key = "{$" + replacement.getKey() + "}";
            String with = replacement.getValue();
            src = src.replace(key, with);
        }
        return src;
    }
}

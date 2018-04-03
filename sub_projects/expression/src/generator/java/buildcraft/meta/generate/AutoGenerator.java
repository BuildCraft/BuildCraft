package buildcraft.meta.generate;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AutoGenerator {

    public static boolean verbose = true;
    private static File outputBase;

    public static void main(String[] args) {

        if (args.length < 2 || !"-out".equals(args[0])) {
            System.out.println("Arguments must be in the form '-base /path/to/base'");
            System.exit(-1);
            return;
        }

        Set<String> flags = new HashSet<>();
        Collections.addAll(flags, args);

        try {
            outputBase = new File(args[1]).getCanonicalFile();
        } catch (IOException io) {
            System.out.println("Couldn't get the canonical version of " + args[1] + "!");
            System.exit(-1);
            return;
        }

        System.out.println("Output directory = " + outputBase);
        if (!outputBase.isDirectory()) {
            System.out.println("(creating output directory)");
            outputBase.mkdirs();
        }
        if (outputBase.list().length != 0) {
            System.out.println("You must clean out the output directory manually!");
            if (args.length > 2) {
                args[2] = "-dryrun";
            }
        }

        if (!flags.contains("-run")) {
            System.out.println("'-run' flag not specified, not writing.");
            outputBase = null;
        }
        verbose = !flags.contains("-quiet");

        // File generators -- add more after this!
        generateFile(new NodeFunc_A_To_B());
        generateFile(new FunctionContextBase_Adder());
        generateFile(new NodeTypeBase_Adder());
    }

    private static void generateFile(AutoGenerateFile gen) {
        gen.generateFiles(outputBase);
    }
}

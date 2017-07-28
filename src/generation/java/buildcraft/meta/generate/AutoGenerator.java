package buildcraft.meta.generate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AutoGenerator {

    private static File bcBase;
    private static File inputBase;
    private static File outputBase;

    public static void main(String[] args) throws IOException {
        if (args.length < 2 || !"-base".equals(args[0])) {
            throw new IllegalArgumentException("Must be in the form '-base /path/to/base'");
        }

        bcBase = new File(args[1]).getCanonicalFile();
        inputBase = new File(bcBase, "src/generation/resources");
        outputBase = new File(bcBase, "src/autogen/java");

        System.out.println("Base directory = " + bcBase);
        System.out.println("Input directory = " + inputBase);
        System.out.println("Output directory = " + outputBase);

        if (!inputBase.isDirectory()) {
            throw new FileNotFoundException("Input Directory");
        }
        if (!outputBase.isDirectory()) {
            throw new FileNotFoundException("Input Directory");
        }
        if (outputBase.list().length != 0) {
            System.out.println("You must clean out the output directory manually!");
            if (args.length > 2) {
                args[2] = "-dryrun";
            }
        }

        if (args.length < 3 || !"-run".equals(args[2])) {
            System.out.println("'-run' flag not specified, not writing.");
            outputBase = null;
        }

        // File generators -- add more after this!
        generateFile(new NodeFunc_A_To_B());
        generateFile(new FunctionContextBase_Adder());
        generateFile(new NodeTypeBase_Adder());
    }

    private static void generateFile(AutoGenerateFile gen) {
        gen.generateFiles(inputBase, outputBase);
    }
}

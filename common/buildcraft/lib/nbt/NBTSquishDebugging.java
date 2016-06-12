package buildcraft.lib.nbt;

import java.util.function.Supplier;

public class NBTSquishDebugging {
    public static boolean debug = false;

    public static void log(Supplier<String> supplier) {
        if (debug) {
            System.out.print(supplier.get() + "\n");
        }
    }
}

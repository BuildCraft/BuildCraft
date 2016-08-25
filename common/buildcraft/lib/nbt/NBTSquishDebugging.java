package buildcraft.lib.nbt;

import java.util.function.Supplier;

public class NBTSquishDebugging {
    public static boolean debug = false;

    @Deprecated
    public static void log(Supplier<String> supplier) {
        if (debug) {
            System.out.print(supplier.get() + "\n");
        }
    }

    public static void log(String string) {
        if (debug) {
            System.out.print(string + "\n");
        }
    }
}

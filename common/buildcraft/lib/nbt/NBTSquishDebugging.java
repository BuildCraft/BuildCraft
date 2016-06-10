package buildcraft.lib.nbt;

import java.util.function.Supplier;

import buildcraft.api.core.BCLog;

public class NBTSquishDebugging {
    public static boolean debug = false;
    public static boolean usesystem;

    public static void log(Supplier<String> supplier) {
        if (debug) {
            if (usesystem) {
                System.out.println(supplier.get());
            } else {
                BCLog.logger.info(supplier.get());
            }
        }
    }
}

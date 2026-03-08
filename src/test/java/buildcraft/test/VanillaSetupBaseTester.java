package buildcraft.test;

import java.io.InputStream;
import java.io.PrintStream;

import net.minecraft.util.registry.Bootstrap;
import org.junit.BeforeClass;

public class VanillaSetupBaseTester {
    @BeforeClass
    public static void init() {
        System.out.println("INIT");
        PrintStream sysOut = System.out;
        InputStream sysIn = System.in;

//        Bootstrap.register();
        Bootstrap.bootStrap();

        System.setIn(sysIn);
        System.setOut(sysOut);
    }
}

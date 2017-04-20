package buildcraft.test;

import java.io.InputStream;
import java.io.PrintStream;

import org.junit.BeforeClass;

import net.minecraft.init.Bootstrap;

public class VanillaSetupBaseTester {
    @BeforeClass
    public static void init() {
        System.out.println("INIT");
        PrintStream sysOut = System.out;
        InputStream sysIn = System.in;

        Bootstrap.register();

        System.setIn(sysIn);
        System.setOut(sysOut);
    }
}

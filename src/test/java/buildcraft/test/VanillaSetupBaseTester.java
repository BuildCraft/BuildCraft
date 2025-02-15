package buildcraft.test;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.BeforeClass;

import java.io.InputStream;
import java.io.PrintStream;

public class VanillaSetupBaseTester {
    @BeforeClass
    public static void init() {
        System.out.println("INIT");
        PrintStream sysOut = System.out;
        InputStream sysIn = System.in;

//        Bootstrap.register();
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        System.setIn(sysIn);
        System.setOut(sysOut);
    }
}

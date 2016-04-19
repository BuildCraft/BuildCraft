package buildcraft.lib;

import buildcraft.api.BCModules;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.item.ItemBuildCraft_BC8;
import buildcraft.lib.list.VanillaListHandlers;
import buildcraft.lib.misc.FakePlayerUtil;

/** Note that this is *not* a mod. However it statically initialises everything in lib. If you depend on lib classes in
 * your mod (and don't depend on bc core) then you *must* call the fml* methods at the appropriate times. */
public class BuildCraftLib {
    private static boolean preInit, init, postInit;

    /** Call this in a static initialiser block in your main mod class. */
    public static void staticInit() {}

    public static void fmlPreInit() {
        if (preInit) return;
        preInit = true;

        BCModules.fmlPreInit();
        BuildCraftAPI.fakePlayerProvider = FakePlayerUtil.INSTANCE;
        LibProxy.getProxy().fmlPreInit();
        BCMessageHandler.preInit();
    }

    public static void fmlInit() {
        if (init) return;
        init = true;

        LibProxy.getProxy().fmlInit();

        VanillaRotationHandlers.fmlInit();
        VanillaListHandlers.fmlInit();

        ItemBuildCraft_BC8.fmlInit();

        BCMessageHandler.init();
    }

    public static void fmlPostInit() {
        if (postInit) return;
        postInit = true;

        LibProxy.getProxy().fmlPostInit();

        VanillaListHandlers.fmlPostInit();
    }
}

package buildcraft.lib;

import buildcraft.lib.item.ItemDebugger;
import buildcraft.lib.item.ItemGuide;
import buildcraft.lib.item.ItemGuideNote;
import buildcraft.lib.item.ItemPropertiesCreator;
import buildcraft.lib.registry.RegistrationHelper;
import net.minecraftforge.registries.RegistryObject;

public class BCLibItems {
    private static final RegistrationHelper HELPER = new RegistrationHelper(BCLib.MODID);

    public static RegistryObject<ItemGuide> guide;
    public static RegistryObject<ItemGuideNote> guideNote;
    public static RegistryObject<ItemDebugger> debugger;

    private static boolean enableGuide, enableDebugger;

    public static void enableGuide() {
        enableGuide = true;
    }

    public static void enableDebugger() {
        enableDebugger = true;
    }

    public static boolean isGuideEnabled() {
        return enableGuide;
    }

    public static boolean isDebuggerEnabled() {
        return enableDebugger;
    }

    /** We should not create register objects in {@link #<cinit>} because guide/debugger are enabled in {@link buildcraft.core.BCCore#<cinit>} */
    public static void fmlPreInit() {
        if (isGuideEnabled()) {
            guide = HELPER.addForcedItem("item.guide", ItemPropertiesCreator.common64(), ItemGuide::new);
            guideNote = HELPER.addForcedItem("item.guide.note", ItemPropertiesCreator.common64(), ItemGuideNote::new);
        }
        if (isDebuggerEnabled()) {
            debugger = HELPER.addForcedItem("item.debugger", ItemPropertiesCreator.common1(), ItemDebugger::new);
        }
    }
}

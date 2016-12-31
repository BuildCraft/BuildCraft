package buildcraft.robotics;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.core.BCCore;
import buildcraft.lib.BCLib;
import buildcraft.lib.BCMessageHandler;
import buildcraft.lib.registry.RegistryHelper;
import buildcraft.robotics.zone.MessageZoneMapRequest;
import buildcraft.robotics.zone.MessageZoneMapResponse;

//@formatter:off
@Mod(modid = BCRobotics.MODID,
name = "BuildCraft Robotics",
version = BCLib.VERSION,
dependencies = "required-after:buildcraftcore@[" + BCLib.VERSION + "]")
//@formatter:on
public class BCRobotics {
    public static final String MODID = "buildcraftrobotics";

    @Mod.Instance(MODID)
    public static BCRobotics INSTANCE = null;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BCCore.MODID);

        BCRoboticsItems.preInit();
        BCRoboticsBlocks.preInit();

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCRoboticsProxy.getProxy());

        BCMessageHandler.addMessageType(MessageZoneMapRequest.class, MessageZoneMapRequest.Handler.INSTANCE, Side.SERVER);
        BCMessageHandler.addMessageType(MessageZoneMapResponse.class, MessageZoneMapResponse.Handler.INSTANCE, Side.CLIENT);
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        BCRoboticsProxy.getProxy().fmlInit();
        BCRoboticsRecipes.init();
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {

    }
}

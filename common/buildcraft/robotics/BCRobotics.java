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
import buildcraft.lib.RegistryHelper;
import buildcraft.robotics.zone.MessageZoneMapRequest;
import buildcraft.robotics.zone.MessageZoneMapResponse;

@Mod(modid = BCRobotics.MODID, name = "BuildCraft Robotics", dependencies = "required-after:buildcraftcore", version = BCLib.VERSION)
public class BCRobotics {
    public static final String MODID = "buildcraftrobotics";

    @Mod.Instance(MODID)
    public static BCRobotics INSTANCE = null;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent evt) {
        RegistryHelper.useOtherModConfigFor(MODID, BCCore.MODID);

        BCRoboticsItems.preInit();
        BCRoboticsBlocks.preInit();

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, RoboticsProxy_BC8.getProxy());

        BCMessageHandler.addMessageType(MessageZoneMapRequest.class, MessageZoneMapRequest.Handler.INSTANCE, Side.SERVER);
        BCMessageHandler.addMessageType(MessageZoneMapResponse.class, MessageZoneMapResponse.Handler.INSTANCE, Side.CLIENT);
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent evt) {
        RoboticsProxy_BC8.getProxy().fmlInit();
        BCRoboticsRecipes.init();
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {

    }
}

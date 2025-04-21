/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import buildcraft.api.BCModules;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.mj.MjAPI;
import buildcraft.core.BCCore;
import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.RegistryConfig;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;
import buildcraft.robotics.boards.*;
import buildcraft.robotics.client.model.RoboticsNodeTypes;
import buildcraft.robotics.client.particle.EntityRobotEnergyParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.util.function.Consumer;

//@formatter:off
//@Mod(
//    modid = BCRobotics.MODID,
//    name = "BuildCraft Robotics",
//    version = BCLib.VERSION,
//    dependencies = "required-after:buildcraftcore@[" + BCLib.VERSION + "]"
//)
@Mod(BCRobotics.MODID)
@Mod.EventBusSubscriber(modid = BCRobotics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
//@formatter:on
public class BCRobotics {
    public static final String MODID = "buildcraftrobotics";

    // @Mod.Instance(MODID)
    public static BCRobotics INSTANCE = null;

    private static CreativeTabManager.CreativeTabBC tabBoards;

    public BCRobotics() {
        INSTANCE = this;
        RoboticsNodeTypes.setup();
    }

    @SubscribeEvent
//    public static void preInit(FMLPreInitializationEvent evt)
    public static void preInit(FMLConstructModEvent evt) {
        RegistryConfig.useOtherModConfigFor(MODID, BCCore.MODID);
        BCRoboticsConfig.preInit();

        tabBoards = CreativeTabManager.createTab("buildcraft.boards");

        RedstoneBoardRegistry.instance = new ImplRedstoneBoardRegistry();
        RedstoneBoardRegistry.instance.setEmptyRobotBoard(RedstoneBoardRobotEmptyNBT.instance);

        // Cheapest, dumbest robot types
        // Those generally do very simple tasks
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_picker", "picker", BoardRobotPicker.class, "green"), 800 * MjAPI.MJ);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_carrier", "carrier", BoardRobotCarrier.class, "green"), 800 * MjAPI.MJ);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_fluid_carrier", "fluid_carrier", BoardRobotFluidCarrier.class, "green"), 800 * MjAPI.MJ);

        // More expensive robot types
        // Those generally handle block mining/harvesting/placement.
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_lumberjack", "lumberjack", BoardRobotLumberjack.class, "blue"), 3200 * MjAPI.MJ);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_harvester", "harvester", BoardRobotHarvester.class, "blue"), 3200 * MjAPI.MJ);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_miner", "miner", BoardRobotMiner.class, "blue"), 3200 * MjAPI.MJ);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_planter", "planter", BoardRobotPlanter.class, "blue"), 3200 * MjAPI.MJ);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_farmer", "farmer", BoardRobotFarmer.class, "blue"), 3200 * MjAPI.MJ);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_leave_cutter", "leave_cutter", BoardRobotLeaveCutter.class, "blue"), 3200 * MjAPI.MJ);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_butcher", "butcher", BoardRobotButcher.class, "blue"), 3200 * MjAPI.MJ);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_shovelman", "shovelman", BoardRobotShovelman.class, "blue"), 3200 * MjAPI.MJ);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_pump", "pump", BoardRobotPump.class, "blue"), 3200 * MjAPI.MJ);

        // Even more expensive
        // These handle complex multi-step operations.
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_delivery", "delivery", BoardRobotDelivery.class, "green"), 12800 * MjAPI.MJ);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_knight", "knight", BoardRobotKnight.class, "red"), 12800 * MjAPI.MJ);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_bomber", "bomber", BoardRobotBomber.class, "red"), 12800 * MjAPI.MJ);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_stripes", "stripes", BoardRobotStripes.class, "yellow"), 12800 * MjAPI.MJ);

        // Most expensive
        // Overpowered galore!
        if (BCModules.BUILDERS.isLoaded()) {
            RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraftrobotics:board_robot_builder", "builder", BoardRobotBuilder.class, "yellow"), 51200 * MjAPI.MJ);
        }

        BCRoboticsBlocks.preInit();
        BCRoboticsPlugs.preInit();
        BCRoboticsItems.preInit();
        BCRoboticsStatements.preInit();
        BCRoboticsEntities.preInit();
        BCRoboticsParticleTypes.preInit();

        BCRoboticsProxy.getProxy().fmlPreInit();

//        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, BCRoboticsProxy.getProxy());
    }

    @SubscribeEvent
//    public static void init(FMLInitializationEvent evt)
    public static void init(FMLCommonSetupEvent evt) {
        BCRoboticsProxy.getProxy().fmlInit();
//        BCRoboticsRecipes.init(); // 1.18.2: datagen
    }

    @SubscribeEvent
//    public static void postInit(FMLPostInitializationEvent evt)
    public static void postInit(FMLLoadCompleteEvent evt) {
        BCRoboticsProxy.getProxy().fmlPostInit();

        tabBoards.setItem(BCRoboticsItems.redstoneBoard.get(RedstoneBoardRegistry.instance.getEmptyRobotBoard()));
    }

    @SubscribeEvent
    public static void registerGui(RegistryEvent.Register<MenuType<?>> event) {
        BCRoboticsMenuTypes.registerAll(event);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particleEngine.register(BCRoboticsParticleTypes.robot.get(), EntityRobotEnergyParticle.Factory::new);
    }

    // Calen: for thread safety
    private static final TagManager tagManager = new TagManager();


    static {
        startBatch();

        // Items
        registerTag("item.robot").reg("robot").locale("robot").tab("buildcraft.boards");
        registerTag("item.redstone_board").reg("redstone_board").locale("redstone_board").tab("buildcraft.boards");
        registerTag("item.plug.robot_station").reg("robot_station").locale("PipeRobotStation").tab("buildcraft.boards");
        registerTag("item.robot_googles").reg("robot_googles").locale("robotGoogles");

        // Item Blocks
        registerTag("item.block.zone_planner").reg("zone_planner").locale("zonePlannerBlock");
//                .model("zone_planner");
        registerTag("item.block.requester").reg("requester").locale("requester");

        // Blocks
//        registerTag("block.zone_planner").reg("zone_planner").oldReg("zonePlannerBlock").locale("zonePlannerBlock").model("zone_planner");
        registerTag("block.zone_planner").reg("zone_planner").locale("zonePlannerBlock");
//                .model("zone_planner");
        registerTag("block.requester").reg("requester").locale("requester");

        // Tiles
        registerTag("tile.zone_planner").reg("zone_planner");
        registerTag("tile.requester").reg("requester");

        // Entities
        registerTag("entity.robot").reg("robot");

//        endBatch(TagManager.prependTags("buildcraftrobotics:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION).andThen(TagManager.setTab("buildcraft.main")));
        endBatch(TagManager.prependTags("buildcraftrobotics:", EnumTagType.REGISTRY_NAME).andThen(TagManager.setTab("buildcraft.main")));
    }

    private static TagEntry registerTag(String id) {
//        return TagManager.registerTag(id);
        return tagManager.registerTag(id);
    }

    private static void startBatch() {
//        TagManager.startBatch();
        tagManager.startBatch();
    }

    private static void endBatch(Consumer<TagEntry> consumer) {
//        TagManager.endBatch(consumer);
        tagManager.endBatch(consumer);
    }
}

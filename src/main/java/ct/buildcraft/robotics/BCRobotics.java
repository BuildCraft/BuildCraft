package ct.buildcraft.robotics;

import ct.buildcraft.api.BCModules;
import ct.buildcraft.api.statements.StatementManager;
import ct.buildcraft.robotics.BCRoboticsStatements;
import ct.buildcraft.robotics.statements.RobotsActionProvider;
import ct.buildcraft.robotics.statements.RobotsTriggerProvider;
import ct.buildcraft.robotics.statements.StatementParameterRobot;
import ct.buildcraft.robotics.statements.StatementParameterMapLocation;
import ct.buildcraft.lib.CreativeTabManager;
import ct.buildcraft.lib.CreativeTabManager.CreativeTabBC;
import ct.buildcraft.robotics.zone.MessageZoneMapRequest;
import ct.buildcraft.robotics.ai.AIRobotBreak;
import ct.buildcraft.robotics.ai.AIRobotAttack;
import ct.buildcraft.robotics.ai.AIRobotHarvest;
import ct.buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import ct.buildcraft.robotics.ai.AIRobotDeliverRequested;
import ct.buildcraft.robotics.ai.AIRobotFetchItem;
import ct.buildcraft.robotics.ai.AIRobotGotoBlock;
import ct.buildcraft.robotics.ai.AIRobotGoAndLinkToDock;
import ct.buildcraft.robotics.ai.AIRobotGotoSleep;
import ct.buildcraft.robotics.ai.AIRobotGotoStation;
import ct.buildcraft.robotics.ai.AIRobotGotoStationAndLoad;
import ct.buildcraft.robotics.ai.AIRobotGotoStationAndLoadFluids;
import ct.buildcraft.robotics.ai.AIRobotGotoStationAndUnload;
import ct.buildcraft.robotics.ai.AIRobotGotoStationAndUnloadFluids;
import ct.buildcraft.robotics.ai.AIRobotGotoStationToLoad;
import ct.buildcraft.robotics.ai.AIRobotGotoStationToLoadFluids;
import ct.buildcraft.robotics.ai.AIRobotGotoStationToUnload;
import ct.buildcraft.robotics.ai.AIRobotGotoStationToUnloadFluids;
import ct.buildcraft.robotics.ai.AIRobotLoad;
import ct.buildcraft.robotics.ai.AIRobotLoadFluids;
import ct.buildcraft.robotics.ai.AIRobotMain;
import ct.buildcraft.robotics.ai.AIRobotPlant;
import ct.buildcraft.robotics.ai.AIRobotPumpBlock;
import ct.buildcraft.robotics.ai.AIRobotRecharge;
import ct.buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import ct.buildcraft.robotics.ai.AIRobotSearchAndGotoStation;
import ct.buildcraft.robotics.ai.AIRobotSearchBlock;
import ct.buildcraft.robotics.ai.AIRobotSearchEntity;
import ct.buildcraft.robotics.ai.AIRobotSearchRandomGroundBlock;
import ct.buildcraft.robotics.ai.AIRobotSearchStackRequest;
import ct.buildcraft.robotics.ai.AIRobotSearchStation;
import ct.buildcraft.robotics.ai.AIRobotShutdown;
import ct.buildcraft.robotics.ai.AIRobotSleep;
import ct.buildcraft.robotics.ai.AIRobotStraightMoveTo;
import ct.buildcraft.robotics.ai.AIRobotUnload;
import ct.buildcraft.robotics.ai.AIRobotUnloadFluids;
import ct.buildcraft.robotics.ai.AIRobotUseToolOnBlock;
import ct.buildcraft.robotics.boards.BoardRobotCarrier;
import ct.buildcraft.robotics.boards.BoardRobotDelivery;
import ct.buildcraft.robotics.boards.BoardRobotFluidCarrier;
import ct.buildcraft.robotics.boards.BoardRobotHarvester;
import ct.buildcraft.robotics.boards.BoardRobotBomber;
import ct.buildcraft.robotics.boards.BoardRobotBuilder;
import ct.buildcraft.robotics.boards.BoardRobotButcher;
import ct.buildcraft.robotics.boards.BoardRobotFarmer;
import ct.buildcraft.robotics.boards.BoardRobotLeaveCutter;
import ct.buildcraft.robotics.boards.BoardRobotKnight;
import ct.buildcraft.robotics.boards.BoardRobotLumberjack;
import ct.buildcraft.robotics.boards.BoardRobotMiner;
import ct.buildcraft.robotics.boards.BoardRobotPicker;
import ct.buildcraft.robotics.boards.BoardRobotPlanter;
import ct.buildcraft.robotics.boards.BoardRobotPump;
import ct.buildcraft.robotics.boards.BoardRobotShovelman;
import ct.buildcraft.api.robots.RobotManager;
import ct.buildcraft.robotics.client.render.RenderRobot;
import ct.buildcraft.robotics.zone.MessageZoneMapResponse;
import ct.buildcraft.robotics.recipes.RobotIntegrationRecipe;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * BuildCraft Robotics bootstrap for the 1.19.2 port.
 *
 * Registers the robotics creative tab, robot items, docking station, boards, zone planner,
 * client/server networking and menu bindings used by the ported robotics systems.
 */
@Mod(BCRobotics.MODID)
public class BCRobotics {
    public static final String MODID = "buildcraftrobotics";

    /** Reuses the historical BuildCraft "boards" tab name, translated as "BuildCraft Robots". */
    public static final CreativeTabBC TAB_ROBOTICS = CreativeTabManager.createTab("buildcraft.boards");

    public BCRobotics() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::init);
        modEventBus.addListener(BCRobotics::registerEntityAttributes);

        BCRoboticsBoards.init();
        BCRoboticsPlugs.preInit();
        BCRoboticsBlocks.registry(modEventBus);
        BCRoboticsItems.registry(modEventBus);
        BCRoboticsEntities.registry(modEventBus);
        BCRoboticsGuis.registry(modEventBus);

        // Keep zone planner network messages available for the partially ported robotics zone code.
        ct.buildcraft.lib.net.MessageManager.registerMessageClass(BCModules.ROBOTICS, MessageZoneMapRequest.class,
                MessageZoneMapRequest.HANDLER, MessageZoneMapRequest::toBytes, MessageZoneMapRequest::new);
        ct.buildcraft.lib.net.MessageManager.registerMessageClass(BCModules.ROBOTICS, MessageZoneMapResponse.class,
                MessageZoneMapResponse.HANDLER, MessageZoneMapResponse::toBytes, MessageZoneMapResponse::new);

        RobotManager.registryProvider = SimpleRobotRegistryProvider.INSTANCE;
        MinecraftForge.EVENT_BUS.register(SimpleRobotRegistryProvider.INSTANCE);
        RobotManager.registerDockingStation(DockingStationPipe.class, "pipe");
        registerRoboticsAI();
        BoardRobotPicker.onServerStart();

        // No config is registered yet; this keeps the module bootstrap deliberately small.
    }

    private void init(final FMLCommonSetupEvent event) {
        // Register robot statement providers and parameter types
        BCRoboticsStatements.preInit();
        StatementManager.registerActionProvider(new RobotsActionProvider());
        StatementManager.registerTriggerProvider(new RobotsTriggerProvider());
        // Register via the reader overload so both NBT persistence and GUI/network buffer sync are available.
        // Work/load area actions store a Map Location item as a parameter; without the buffer reader, gates can save
        // the parameter but fail to sync/open correctly when that action is configured.
        StatementManager.registerParameter(StatementParameterRobot::readFromNbt);
        StatementManager.registerParameter(StatementParameterMapLocation::readFromNbt);

        BCRoboticsBoards.init();
        BCRoboticsPlugs.preInit();
        RobotIntegrationRecipe.register();
        RobotManager.registryProvider = SimpleRobotRegistryProvider.INSTANCE;
        TAB_ROBOTICS.setItem(BCRoboticsItems.ROBOT.get());
    }

    private static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(BCRoboticsEntities.ROBOT.get(), ct.buildcraft.robotics.entity.EntityRobot.createAttributes().build());
    }


    private static void registerRoboticsAI() {
        if (RobotManager.getAIRobotName(AIRobotMain.class) != null) {
            return;
        }
        RobotManager.registerAIRobot(AIRobotMain.class, "main", "buildcraft.robotics.ai.AIRobotMain");
        RobotManager.registerAIRobot(BoardRobotPicker.class, "boardPicker", "buildcraft.robotics.boards.BoardRobotPicker");
        RobotManager.registerAIRobot(BoardRobotCarrier.class, "boardCarrier", "buildcraft.robotics.boards.BoardRobotCarrier");
        RobotManager.registerAIRobot(BoardRobotFluidCarrier.class, "boardFluidCarrier", "buildcraft.robotics.boards.BoardRobotFluidCarrier");
        RobotManager.registerAIRobot(BoardRobotLumberjack.class, "boardLumberjack", "buildcraft.robotics.boards.BoardRobotLumberjack");
        RobotManager.registerAIRobot(BoardRobotHarvester.class, "boardHarvester", "buildcraft.robotics.boards.BoardRobotHarvester");
        RobotManager.registerAIRobot(BoardRobotMiner.class, "boardMiner", "buildcraft.robotics.boards.BoardRobotMiner");
        RobotManager.registerAIRobot(BoardRobotPlanter.class, "boardPlanter", "buildcraft.robotics.boards.BoardRobotPlanter");
        RobotManager.registerAIRobot(BoardRobotFarmer.class, "boardFarmer", "buildcraft.robotics.boards.BoardRobotFarmer");
        RobotManager.registerAIRobot(BoardRobotLeaveCutter.class, "boardLeaveCutter", "buildcraft.robotics.boards.BoardRobotLeaveCutter");
        RobotManager.registerAIRobot(BoardRobotButcher.class, "boardButcher", "buildcraft.robotics.boards.BoardRobotButcher");
        RobotManager.registerAIRobot(BoardRobotShovelman.class, "boardShovelman", "buildcraft.robotics.boards.BoardRobotShovelman");
        RobotManager.registerAIRobot(BoardRobotPump.class, "boardPump", "buildcraft.robotics.boards.BoardRobotPump");
        RobotManager.registerAIRobot(BoardRobotDelivery.class, "boardRobotDelivery", "buildcraft.robotics.boards.BoardRobotDelivery");
        RobotManager.registerAIRobot(BoardRobotKnight.class, "boardKnight", "buildcraft.robotics.boards.BoardRobotKnight");
        RobotManager.registerAIRobot(BoardRobotBomber.class, "boardBomber", "buildcraft.robotics.boards.BoardRobotBomber");
        RobotManager.registerAIRobot(BoardRobotBuilder.class, "boardBuilder", "buildcraft.robotics.boards.BoardRobotBuilder");
        RobotManager.registerAIRobot(AIRobotFetchItem.class, "fetchItem", "buildcraft.robotics.ai.AIRobotFetchItem");
        RobotManager.registerAIRobot(AIRobotFetchAndEquipItemStack.class, "fetchAndEquipItemStack", "buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack");
        RobotManager.registerAIRobot(AIRobotSearchBlock.class, "searchBlock", "buildcraft.robotics.ai.AIRobotSearchBlock");
        RobotManager.registerAIRobot(AIRobotSearchRandomGroundBlock.class, "searchRandomGroundBlock", "buildcraft.robotics.ai.AIRobotSearchRandomGroundBlock");
        RobotManager.registerAIRobot(AIRobotSearchEntity.class, "searchEntity", "buildcraft.robotics.ai.AIRobotSearchEntity");
        RobotManager.registerAIRobot(AIRobotSearchAndGotoBlock.class, "searchAndGotoBlock", "buildcraft.robotics.ai.AIRobotSearchAndGotoBlock");
        RobotManager.registerAIRobot(AIRobotBreak.class, "break", "buildcraft.robotics.ai.AIRobotBreak");
        RobotManager.registerAIRobot(AIRobotPumpBlock.class, "pumpBlock", "buildcraft.robotics.ai.AIRobotPumpBlock");
        RobotManager.registerAIRobot(AIRobotAttack.class, "attack", "buildcraft.robotics.ai.AIRobotAttack");
        RobotManager.registerAIRobot(AIRobotHarvest.class, "harvest", "buildcraft.robotics.ai.AIRobotHarvest");
        RobotManager.registerAIRobot(AIRobotPlant.class, "plant", "buildcraft.robotics.ai.AIRobotPlant");
        RobotManager.registerAIRobot(AIRobotUseToolOnBlock.class, "useToolOnBlock", "buildcraft.robotics.ai.AIRobotUseToolOnBlock");
        RobotManager.registerAIRobot(AIRobotGotoBlock.class, "gotoBlock", "buildcraft.robotics.ai.AIRobotGotoBlock");
        RobotManager.registerAIRobot(AIRobotStraightMoveTo.class, "straightMoveTo", "buildcraft.robotics.ai.AIRobotStraightMoveTo");
        RobotManager.registerAIRobot(AIRobotGotoStation.class, "gotoStation", "buildcraft.robotics.ai.AIRobotGotoStation");
        RobotManager.registerAIRobot(AIRobotGoAndLinkToDock.class, "goAndLinkToDock", "buildcraft.robotics.ai.AIRobotGoAndLinkToDock");
        RobotManager.registerAIRobot(AIRobotGotoStationToLoad.class, "gotoStationToLoad", "buildcraft.robotics.ai.AIRobotGotoStationToLoad");
        RobotManager.registerAIRobot(AIRobotGotoStationAndLoad.class, "gotoStationAndLoad", "buildcraft.robotics.ai.AIRobotGotoStationAndLoad");
        RobotManager.registerAIRobot(AIRobotGotoStationToLoadFluids.class, "gotoStationToLoadFluids", "buildcraft.robotics.ai.AIRobotGotoStationToLoadFluids");
        RobotManager.registerAIRobot(AIRobotGotoStationAndLoadFluids.class, "gotoStationAndLoadFluids", "buildcraft.robotics.ai.AIRobotGotoStationAndLoadFluids");
        RobotManager.registerAIRobot(AIRobotGotoStationToUnload.class, "gotoStationToUnload", "buildcraft.robotics.ai.AIRobotGotoStationToUnload");
        RobotManager.registerAIRobot(AIRobotGotoStationAndUnload.class, "gotoStationAndUnload", "buildcraft.robotics.ai.AIRobotGotoStationAndUnload");
        RobotManager.registerAIRobot(AIRobotGotoStationToUnloadFluids.class, "gotoStationToUnloadFluids", "buildcraft.robotics.ai.AIRobotGotoStationToUnloadFluids");
        RobotManager.registerAIRobot(AIRobotGotoStationAndUnloadFluids.class, "gotoStationAndUnloadFluids", "buildcraft.robotics.ai.AIRobotGotoStationAndUnloadFluids");
        RobotManager.registerAIRobot(AIRobotSearchStackRequest.class, "searchStackRequest", "buildcraft.robotics.ai.AIRobotSearchStackRequest");
        RobotManager.registerAIRobot(AIRobotSearchStation.class, "searchStation", "buildcraft.robotics.ai.AIRobotSearchStation");
        RobotManager.registerAIRobot(AIRobotSearchAndGotoStation.class, "searchAndGotoStation", "buildcraft.robotics.ai.AIRobotSearchAndGotoStation");
        RobotManager.registerAIRobot(AIRobotLoad.class, "load", "buildcraft.robotics.ai.AIRobotLoad");
        RobotManager.registerAIRobot(AIRobotLoadFluids.class, "loadFluids", "buildcraft.robotics.ai.AIRobotLoadFluids");
        RobotManager.registerAIRobot(AIRobotDeliverRequested.class, "deliverRequested", "buildcraft.robotics.ai.AIRobotDeliverRequested");
        RobotManager.registerAIRobot(AIRobotUnload.class, "unload", "buildcraft.robotics.ai.AIRobotUnload");
        RobotManager.registerAIRobot(AIRobotUnloadFluids.class, "unloadFluids", "buildcraft.robotics.ai.AIRobotUnloadFluids");
        RobotManager.registerAIRobot(AIRobotGotoSleep.class, "gotoSleep", "buildcraft.robotics.ai.AIRobotGotoSleep");
        RobotManager.registerAIRobot(AIRobotSleep.class, "sleep", "buildcraft.robotics.ai.AIRobotSleep");
        RobotManager.registerAIRobot(AIRobotRecharge.class, "recharge", "buildcraft.robotics.ai.AIRobotRecharge");
        RobotManager.registerAIRobot(AIRobotShutdown.class, "shutdown", "buildcraft.robotics.ai.AIRobotShutdown");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        private static final ResourceLocation ROBOT_MODEL = new ResourceLocation(MODID, "robot");
        private static final ResourceLocation BOARD_MODEL = new ResourceLocation(MODID, "board");

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            BCRoboticsSprites.preInit();
            event.enqueueWork(() -> {
                ItemProperties.register(BCRoboticsItems.ROBOT.get(), ROBOT_MODEL,
                        (stack, level, entity, seed) -> BCRoboticsBoards.getRobotModelValue(stack));
                ItemProperties.register(BCRoboticsItems.REDSTONE_BOARD.get(), BOARD_MODEL,
                        (stack, level, entity, seed) -> BCRoboticsBoards.getBoardModelValue(stack));
                BCRoboticsModels.init();
            });
        }

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(BCRoboticsEntities.ROBOT.get(), RenderRobot::new);
        }


        @SubscribeEvent
        public static void onModelBake(BakingCompleted event) {
            BCRoboticsModels.onModelBake(event);
        }
    }
}

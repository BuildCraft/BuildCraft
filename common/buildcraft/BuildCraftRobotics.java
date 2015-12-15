/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.lists.ListRegistry;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.*;
import buildcraft.core.config.ConfigManager;
import buildcraft.core.network.EntityIds;
import buildcraft.robotics.*;
import buildcraft.robotics.ai.*;
import buildcraft.robotics.boards.*;
import buildcraft.robotics.map.MapManager;
import buildcraft.robotics.render.RobotItemModel;
import buildcraft.robotics.statements.*;
import buildcraft.robotics.statements.ActionRobotWorkInArea.AreaType;
import buildcraft.silicon.ItemRedstoneChipset;

@Mod(name = "BuildCraft Robotics", version = DefaultProps.VERSION, useMetadata = false, modid = "BuildCraft|Robotics",
        dependencies = DefaultProps.DEPENDENCY_TRANSPORT)
public class BuildCraftRobotics extends BuildCraftMod {
    @Mod.Instance("BuildCraft|Robotics")
    public static BuildCraftRobotics instance;

    public static BlockZonePlan zonePlanBlock;
    public static BlockRequester requesterBlock;

    public static ItemRedstoneBoard redstoneBoard;
    public static Item robotItem;
    public static Item robotStationItem;

    public static ITriggerInternal triggerRobotSleep = new TriggerRobotSleep();
    public static ITriggerInternal triggerRobotInStation = new TriggerRobotInStation();
    public static ITriggerInternal triggerRobotLinked = new TriggerRobotLinked(false);
    public static ITriggerInternal triggerRobotReserved = new TriggerRobotLinked(true);

    public static IActionInternal actionRobotGotoStation = new ActionRobotGotoStation();
    public static IActionInternal actionRobotWakeUp = new ActionRobotWakeUp();
    public static IActionInternal actionRobotWorkInArea = new ActionRobotWorkInArea(AreaType.WORK);
    public static IActionInternal actionRobotLoadUnloadArea = new ActionRobotWorkInArea(AreaType.LOAD_UNLOAD);
    public static IActionInternal actionRobotFilter = new ActionRobotFilter();
    public static IActionInternal actionRobotFilterTool = new ActionRobotFilterTool();
    public static IActionInternal actionStationRequestItems = new ActionStationRequestItems();
    public static IActionInternal actionStationProvideItems = new ActionStationProvideItems();
    public static IActionInternal actionStationAcceptFluids = new ActionStationAcceptFluids();
    public static IActionInternal actionStationProvideFluids = new ActionStationProvideFluids();
    public static IActionInternal actionStationForceRobot = new ActionStationForbidRobot(true);
    public static IActionInternal actionStationForbidRobot = new ActionStationForbidRobot(false);
    public static IActionInternal actionStationAcceptItems = new ActionStationAcceptItems();
    public static IActionInternal actionStationMachineRequestItems = new ActionStationRequestItemsMachine();

    public static List<String> blacklistedRobots;

    public static MapManager manager;
    private static Thread managerThread;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        new BCCreativeTab("boards");

        BuildCraftCore.mainConfigManager.register("general", "boards.blacklist", new String[] {}, "Blacklisted robots boards",
                ConfigManager.RestartRequirement.GAME);

        reloadConfig(ConfigManager.RestartRequirement.GAME);

        robotItem = new ItemRobot().setUnlocalizedName("robot");
        BCRegistry.INSTANCE.registerItem(robotItem, false);

        robotStationItem = new ItemRobotStation().setUnlocalizedName("robotStation");
        BCRegistry.INSTANCE.registerItem(robotStationItem, false);

        redstoneBoard = new ItemRedstoneBoard();
        redstoneBoard.setUnlocalizedName("redstone_board");
        BCRegistry.INSTANCE.registerItem(redstoneBoard, false);

        zonePlanBlock = (BlockZonePlan) CompatHooks.INSTANCE.getBlock(BlockZonePlan.class);
        zonePlanBlock.setUnlocalizedName("zonePlan");
        BCRegistry.INSTANCE.registerBlock(zonePlanBlock, false);

        requesterBlock = (BlockRequester) CompatHooks.INSTANCE.getBlock(BlockRequester.class);
        requesterBlock.setUnlocalizedName("requester");
        BCRegistry.INSTANCE.registerBlock(requesterBlock, false);

        RedstoneBoardRegistry.instance = new ImplRedstoneBoardRegistry();

        RedstoneBoardRegistry.instance.setEmptyRobotBoard(RedstoneBoardRobotEmptyNBT.instance);

        // Cheapest, dumbest robot types
        // Those generally do very simple tasks
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotPicker", "picker", BoardRobotPicker.class, "green"),
                8000);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotCarrier", "carrier", BoardRobotCarrier.class, "green"),
                8000);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotFluidCarrier", "fluidCarrier",
                BoardRobotFluidCarrier.class, "green"), 8000);

        // More expensive robot types
        // Those generally handle block mining/harvesting/placement.
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotLumberjack", "lumberjack", BoardRobotLumberjack.class,
                "blue"), 32000);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotHarvester", "harvester", BoardRobotHarvester.class,
                "blue"), 32000);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:miner", "miner", BoardRobotMiner.class, "blue"), 32000);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotPlanter", "planter", BoardRobotPlanter.class, "blue"),
                32000);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotFarmer", "farmer", BoardRobotFarmer.class, "blue"),
                32000);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:leave_cutter", "leaveCutter", BoardRobotLeaveCutter.class,
                "blue"), 32000);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotButcher", "butcher", BoardRobotButcher.class, "blue"),
                32000);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:shovelman", "shovelman", BoardRobotShovelman.class, "blue"),
                32000);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotPump", "pump", BoardRobotPump.class, "blue"), 32000);

        // Even more expensive
        // These handle complex multi-step operations.
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotDelivery", "delivery", BoardRobotDelivery.class,
                "green"), 128000);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotKnight", "knight", BoardRobotKnight.class, "red"),
                128000);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotBomber", "bomber", BoardRobotBomber.class, "red"),
                128000);
        RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotStripes", "stripes", BoardRobotStripes.class, "yellow"),
                128000);

        // Most expensive
        // Overpowered galore!
        if (Loader.isModLoaded("BuildCraft|Builders")) {
            RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotBuilder", "builder", BoardRobotBuilder.class,
                    "yellow"), 512000);
        }

        StatementManager.registerParameterClass(StatementParameterRobot.class);
        StatementManager.registerParameterClass(StatementParameterMapLocation.class);
        StatementManager.registerActionProvider(new RobotsActionProvider());
        StatementManager.registerTriggerProvider(new RobotsTriggerProvider());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new RoboticsGuiHandler());
        MinecraftForge.EVENT_BUS.register(this);

        if (BuildCraftCore.loadDefaultRecipes && Loader.isModLoaded("BuildCraft|Silicon")) {
            loadRecipes();
        }

        BCCreativeTab.get("boards").setIcon(new ItemStack(BuildCraftRobotics.redstoneBoard, 1));

        BuilderAPI.schematicRegistry.registerSchematicBlock(requesterBlock, SchematicTile.class);

        PipeManager.registerPipePluggable(RobotStationPluggable.class, "robotStation");
        EntityRegistry.registerModEntity(EntityRobot.class, "bcRobot", EntityIds.ROBOT, instance, 50, 1, true);

        BCRegistry.INSTANCE.registerTileEntity(TileZonePlan.class, "net.minecraft.src.buildcraft.commander.TileZonePlan");
        BCRegistry.INSTANCE.registerTileEntity(TileRequester.class, "net.minecraft.src.buildcraft.commander.TileRequester");

        RobotManager.registryProvider = new RobotRegistryProvider();

        RobotManager.registerAIRobot(AIRobotMain.class, "aiRobotMain", "buildcraft.core.robots.AIRobotMain");
        RobotManager.registerAIRobot(BoardRobotEmpty.class, "boardRobotEmpty");
        RobotManager.registerAIRobot(BoardRobotBomber.class, "boardRobotBomber", "buildcraft.core.robots.boards.BoardRobotBomber");
        if (Loader.isModLoaded("BuildCraft|Builders")) {
            RobotManager.registerAIRobot(BoardRobotBuilder.class, "boardRobotBuilder", "buildcraft.core.robots.boards.BoardRobotBuilder");
        }
        RobotManager.registerAIRobot(BoardRobotButcher.class, "boardRobotButcher", "buildcraft.core.robots.boards.BoardRobotButcher");
        RobotManager.registerAIRobot(BoardRobotCarrier.class, "boardRobotCarrier", "buildcraft.core.robots.boards.BoardRobotCarrier");
        RobotManager.registerAIRobot(BoardRobotDelivery.class, "boardRobotDelivery", "buildcraft.core.robots.boards.BoardRobotDelivery");
        RobotManager.registerAIRobot(BoardRobotFarmer.class, "boardRobotFarmer", "buildcraft.core.robots.boards.BoardRobotFarmer");
        RobotManager.registerAIRobot(BoardRobotFluidCarrier.class, "boardRobotFluidCarrier", "buildcraft.core.robots.boards.BoardRobotFluidCarrier");
        RobotManager.registerAIRobot(BoardRobotHarvester.class, "boardRobotHarvester", "buildcraft.core.robots.boards.BoardRobotHarvester");
        RobotManager.registerAIRobot(BoardRobotKnight.class, "boardRobotKnight", "buildcraft.core.robots.boards.BoardRobotKnight");
        RobotManager.registerAIRobot(BoardRobotLeaveCutter.class, "boardRobotLeaveCutter", "buildcraft.core.robots.boards.BoardRobotLeaveCutter");
        RobotManager.registerAIRobot(BoardRobotLumberjack.class, "boardRobotLumberjack", "buildcraft.core.robots.boards.BoardRobotLumberjack");
        RobotManager.registerAIRobot(BoardRobotMiner.class, "boardRobotMiner", "buildcraft.core.robots.boards.BoardRobotMiner");
        RobotManager.registerAIRobot(BoardRobotPicker.class, "boardRobotPicker", "buildcraft.core.robots.boards.BoardRobotPicker");
        RobotManager.registerAIRobot(BoardRobotPlanter.class, "boardRobotPlanter", "buildcraft.core.robots.boards.BoardRobotPlanter");
        RobotManager.registerAIRobot(BoardRobotPump.class, "boardRobotPump", "buildcraft.core.robots.boards.BoardRobotPump");
        RobotManager.registerAIRobot(BoardRobotShovelman.class, "boardRobotShovelman", "buildcraft.core.robots.boards.BoardRobotShovelman");
        RobotManager.registerAIRobot(BoardRobotStripes.class, "boardRobotStripes", "buildcraft.core.robots.boards.BoardRobotStripes");
        RobotManager.registerAIRobot(AIRobotAttack.class, "aiRobotAttack", "buildcraft.core.robots.AIRobotAttack");
        RobotManager.registerAIRobot(AIRobotBreak.class, "aiRobotBreak", "buildcraft.core.robots.AIRobotBreak");
        RobotManager.registerAIRobot(AIRobotDeliverRequested.class, "aiRobotDeliverRequested", "buildcraft.core.robots.AIRobotDeliverRequested");
        RobotManager.registerAIRobot(AIRobotDisposeItems.class, "aiRobotDisposeItems", "buildcraft.core.robots.AIRobotDisposeItems");
        RobotManager.registerAIRobot(AIRobotFetchAndEquipItemStack.class, "aiRobotFetchAndEquipItemStack",
                "buildcraft.core.robots.AIRobotFetchAndEquipItemStack");
        RobotManager.registerAIRobot(AIRobotFetchItem.class, "aiRobotFetchItem", "buildcraft.core.robots.AIRobotFetchItem");
        RobotManager.registerAIRobot(AIRobotGoAndLinkToDock.class, "aiRobotGoAndLinkToDock", "buildcraft.core.robots.AIRobotGoAndLinkToDock");
        RobotManager.registerAIRobot(AIRobotGoto.class, "aiRobotGoto", "buildcraft.core.robots.AIRobotGoto");
        RobotManager.registerAIRobot(AIRobotGotoBlock.class, "aiRobotGotoBlock", "buildcraft.core.robots.AIRobotGotoBlock");
        RobotManager.registerAIRobot(AIRobotGotoSleep.class, "aiRobotGotoSleep", "buildcraft.core.robots.AIRobotGotoSleep");
        RobotManager.registerAIRobot(AIRobotGotoStation.class, "aiRobotGotoStation", "buildcraft.core.robots.AIRobotGotoStation");
        RobotManager.registerAIRobot(AIRobotGotoStationAndLoad.class, "aiRobotGotoStationAndLoad",
                "buildcraft.core.robots.AIRobotGotoStationAndLoad");
        RobotManager.registerAIRobot(AIRobotGotoStationAndLoadFluids.class, "aiRobotGotoStationAndLoadFluids",
                "buildcraft.core.robots.AIRobotGotoStationAndLoadFluids");
        RobotManager.registerAIRobot(AIRobotGotoStationAndUnload.class, "aiRobotGotoStationAndUnload",
                "buildcraft.core.robots.AIRobotGotoStationAndUnload");
        RobotManager.registerAIRobot(AIRobotGotoStationToLoad.class, "aiRobotGotoStationToLoad", "buildcraft.core.robots.AIRobotGotoStationToLoad");
        RobotManager.registerAIRobot(AIRobotGotoStationToLoadFluids.class, "aiRobotGotoStationToLoadFluids",
                "buildcraft.core.robots.AIRobotGotoStationToLoadFluids");
        RobotManager.registerAIRobot(AIRobotGotoStationToUnload.class, "aiRobotGotoStationToUnload",
                "buildcraft.core.robots.AIRobotGotoStationToUnload");
        RobotManager.registerAIRobot(AIRobotGotoStationToUnloadFluids.class, "aiRobotGotoStationToUnloadFluids",
                "buildcraft.core.robots.AIRobotGotoStationToUnloadFluids");
        RobotManager.registerAIRobot(AIRobotHarvest.class, "aiRobotHarvest");
        RobotManager.registerAIRobot(AIRobotLoad.class, "aiRobotLoad", "buildcraft.core.robots.AIRobotLoad");
        RobotManager.registerAIRobot(AIRobotLoadFluids.class, "aiRobotLoadFluids", "buildcraft.core.robots.AIRobotLoadFluids");
        RobotManager.registerAIRobot(AIRobotPlant.class, "aiRobotPlant");
        RobotManager.registerAIRobot(AIRobotPumpBlock.class, "aiRobotPumpBlock", "buildcraft.core.robots.AIRobotPumpBlock");
        RobotManager.registerAIRobot(AIRobotRecharge.class, "aiRobotRecharge", "buildcraft.core.robots.AIRobotRecharge");
        RobotManager.registerAIRobot(AIRobotSearchAndGotoBlock.class, "aiRobotSearchAndGoToBlock");
        RobotManager.registerAIRobot(AIRobotSearchAndGotoStation.class, "aiRobotSearchAndGotoStation",
                "buildcraft.core.robots.AIRobotSearchAndGotoStation");
        RobotManager.registerAIRobot(AIRobotSearchBlock.class, "aiRobotSearchBlock", "buildcraft.core.robots.AIRobotSearchBlock");
        RobotManager.registerAIRobot(AIRobotSearchEntity.class, "aiRobotSearchEntity", "buildcraft.core.robots.AIRobotSearchEntity");
        RobotManager.registerAIRobot(AIRobotSearchRandomGroundBlock.class, "aiRobotSearchRandomGroundBlock",
                "buildcraft.core.robots.AIRobotSearchRandomGroundBlock");
        RobotManager.registerAIRobot(AIRobotSearchStackRequest.class, "aiRobotSearchStackRequest",
                "buildcraft.core.robots.AIRobotSearchStackRequest");
        RobotManager.registerAIRobot(AIRobotSearchStation.class, "aiRobotSearchStation", "buildcraft.core.robots.AIRobotSearchStation");
        RobotManager.registerAIRobot(AIRobotShutdown.class, "aiRobotShutdown");
        RobotManager.registerAIRobot(AIRobotSleep.class, "aiRobotSleep", "buildcraft.core.robots.AIRobotSleep");
        RobotManager.registerAIRobot(AIRobotStraightMoveTo.class, "aiRobotStraightMoveTo", "buildcraft.core.robots.AIRobotStraightMoveTo");
        RobotManager.registerAIRobot(AIRobotUnload.class, "aiRobotUnload", "buildcraft.core.robots.AIRobotUnload");
        RobotManager.registerAIRobot(AIRobotUnloadFluids.class, "aiRobotUnloadFluids", "buildcraft.core.robots.AIRobotUnloadFluids");
        RobotManager.registerAIRobot(AIRobotUseToolOnBlock.class, "aiRobotUseToolOnBlock", "buildcraft.core.robots.AIRobotUseToolOnBlock");

        RobotManager.registerDockingStation(DockingStationPipe.class, "dockingStationPipe");

        RoboticsProxy.proxy.init();

        ListRegistry.itemClassAsType.add(ItemRobot.class);
    }

    public static void loadRecipes() {
        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(robotItem), "PPP", "PRP", "C C", 'P', "ingotIron", 'R', "crystalRedstone", 'C',
                ItemRedstoneChipset.Chipset.DIAMOND.getStack());

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(redstoneBoard), "PPP", "PRP", "PPP", 'R', "dustRedstone", 'P', Items.paper);

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(zonePlanBlock, 1, 0), "IRI", "GMG", "IDI", 'M', Items.map, 'R', "dustRedstone", 'G',
                "gearGold", 'D', "gearDiamond", 'I', "ingotIron");

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(requesterBlock, 1, 0), "IPI", "GCG", "IRI", 'C', "chestWood", 'R', "dustRedstone", 'P',
                Blocks.piston, 'G', "gearIron", 'I', "ingotIron");

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(robotStationItem), "   ", " I ", "ICI", 'I', "ingotIron", 'C',
                ItemRedstoneChipset.Chipset.GOLD.getStack());

        BuildcraftRecipeRegistry.programmingTable.addRecipe(new BoardProgrammingRecipe());
        BuildcraftRecipeRegistry.integrationTable.addRecipe(new RobotIntegrationRecipe());
    }

    private void stopMapManager() {
        if (manager != null) {
            manager.stop();
            MinecraftForge.EVENT_BUS.unregister(manager);
            MinecraftForge.EVENT_BUS.unregister(manager);
        }

        if (managerThread != null) {
            managerThread.interrupt();
        }

        managerThread = null;
        manager = null;
    }

    @Mod.EventHandler
    public void serverUnload(FMLServerStoppingEvent event) {
        stopMapManager();
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        File f = new File(DimensionManager.getCurrentSaveRootDirectory(), "buildcraft/zonemap");

        try {
            f.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }

        stopMapManager();

        manager = new MapManager(f);
        managerThread = new Thread(manager);
        managerThread.start();

        BoardRobotPicker.onServerStart();

        MinecraftForge.EVENT_BUS.register(manager);
    }

    @Mod.EventHandler
    public void serverLoadFinish(FMLServerStartedEvent event) {
        manager.initialize();
    }

    @Mod.EventHandler
    public void processRequests(FMLInterModComms.IMCEvent event) {
        InterModComms.processIMC(event);
    }

    public void reloadConfig(ConfigManager.RestartRequirement restartType) {
        if (restartType == ConfigManager.RestartRequirement.GAME) {

            blacklistedRobots = new ArrayList<String>();
            blacklistedRobots.addAll(Arrays.asList(BuildCraftCore.mainConfigManager.get("general", "boards.blacklist").getStringList()));
            reloadConfig(ConfigManager.RestartRequirement.WORLD);
        } else if (restartType == ConfigManager.RestartRequirement.WORLD) {
            reloadConfig(ConfigManager.RestartRequirement.NONE);
        } else {
            if (BuildCraftCore.mainConfiguration.hasChanged()) {
                BuildCraftCore.mainConfiguration.save();
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if ("BuildCraft|Core".equals(event.modID)) {
            reloadConfig(event.isWorldRunning ? ConfigManager.RestartRequirement.NONE : ConfigManager.RestartRequirement.WORLD);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelBakeEvent event) {
        ModelResourceLocation mrl = new ModelResourceLocation("buildcraftrobotics:robot", "inventory");
        event.modelRegistry.putObject(mrl, RobotItemModel.create());
    }
}

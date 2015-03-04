/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.network.EntityIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.robotics.BoardProgrammingRecipe;
import buildcraft.robotics.EntityRobot;
import buildcraft.robotics.ImplRedstoneBoardRegistry;
import buildcraft.robotics.ItemRedstoneBoard;
import buildcraft.robotics.ItemRobot;
import buildcraft.robotics.RobotRegistry;
import buildcraft.robotics.RobotRegistryProvider;
import buildcraft.silicon.ResourceIdAssemblyTable;
import buildcraft.robotics.ResourceIdBlock;
import buildcraft.robotics.ResourceIdRequest;
import buildcraft.robotics.RobotIntegrationRecipe;
import buildcraft.robotics.RoboticsProxy;
import buildcraft.robotics.ai.AIRobotAttack;
import buildcraft.robotics.ai.AIRobotBreak;
import buildcraft.robotics.ai.AIRobotCraftAssemblyTable;
import buildcraft.robotics.ai.AIRobotCraftFurnace;
import buildcraft.robotics.ai.AIRobotCraftWorkbench;
import buildcraft.robotics.ai.AIRobotDeliverRequested;
import buildcraft.robotics.ai.AIRobotDisposeItems;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robotics.ai.AIRobotFetchItem;
import buildcraft.robotics.ai.AIRobotGoAndLinkToDock;
import buildcraft.robotics.ai.AIRobotGoto;
import buildcraft.robotics.ai.AIRobotGotoBlock;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStation;
import buildcraft.robotics.ai.AIRobotGotoStationAndLoad;
import buildcraft.robotics.ai.AIRobotGotoStationAndLoadFluids;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnload;
import buildcraft.robotics.ai.AIRobotGotoStationToLoad;
import buildcraft.robotics.ai.AIRobotGotoStationToLoadFluids;
import buildcraft.robotics.ai.AIRobotGotoStationToUnload;
import buildcraft.robotics.ai.AIRobotGotoStationToUnloadFluids;
import buildcraft.robotics.ai.AIRobotLoad;
import buildcraft.robotics.ai.AIRobotLoadFluids;
import buildcraft.robotics.ai.AIRobotMain;
import buildcraft.robotics.ai.AIRobotPumpBlock;
import buildcraft.robotics.ai.AIRobotRecharge;
import buildcraft.robotics.ai.AIRobotSearchAndGotoStation;
import buildcraft.robotics.ai.AIRobotSearchBlock;
import buildcraft.robotics.ai.AIRobotSearchEntity;
import buildcraft.robotics.ai.AIRobotSearchRandomBlock;
import buildcraft.robotics.ai.AIRobotSearchRandomGroundBlock;
import buildcraft.robotics.ai.AIRobotSearchStackRequest;
import buildcraft.robotics.ai.AIRobotSearchStation;
import buildcraft.robotics.ai.AIRobotSleep;
import buildcraft.robotics.ai.AIRobotStraightMoveTo;
import buildcraft.robotics.ai.AIRobotUnload;
import buildcraft.robotics.ai.AIRobotUnloadFluids;
import buildcraft.robotics.ai.AIRobotUseToolOnBlock;
import buildcraft.robotics.boards.BoardRobotBomber;
import buildcraft.robotics.boards.BoardRobotBomberNBT;
import buildcraft.robotics.boards.BoardRobotBuilder;
import buildcraft.robotics.boards.BoardRobotBuilderNBT;
import buildcraft.robotics.boards.BoardRobotButcher;
import buildcraft.robotics.boards.BoardRobotButcherNBT;
import buildcraft.robotics.boards.BoardRobotCarrier;
import buildcraft.robotics.boards.BoardRobotCarrierNBT;
import buildcraft.robotics.boards.BoardRobotCrafter;
import buildcraft.robotics.boards.BoardRobotCrafterNBT;
import buildcraft.robotics.boards.BoardRobotDelivery;
import buildcraft.robotics.boards.BoardRobotDeliveryNBT;
import buildcraft.robotics.boards.BoardRobotFarmer;
import buildcraft.robotics.boards.BoardRobotFarmerNBT;
import buildcraft.robotics.boards.BoardRobotFluidCarrier;
import buildcraft.robotics.boards.BoardRobotFluidCarrierNBT;
import buildcraft.robotics.boards.BoardRobotHarvester;
import buildcraft.robotics.boards.BoardRobotHarvesterNBT;
import buildcraft.robotics.boards.BoardRobotKnight;
import buildcraft.robotics.boards.BoardRobotKnightNBT;
import buildcraft.robotics.boards.BoardRobotLeaveCutter;
import buildcraft.robotics.boards.BoardRobotLeaveCutterNBT;
import buildcraft.robotics.boards.BoardRobotLumberjack;
import buildcraft.robotics.boards.BoardRobotLumberjackNBT;
import buildcraft.robotics.boards.BoardRobotMiner;
import buildcraft.robotics.boards.BoardRobotMinerNBT;
import buildcraft.robotics.boards.BoardRobotPicker;
import buildcraft.robotics.boards.BoardRobotPickerNBT;
import buildcraft.robotics.boards.BoardRobotPlanter;
import buildcraft.robotics.boards.BoardRobotPlanterNBT;
import buildcraft.robotics.boards.BoardRobotPump;
import buildcraft.robotics.boards.BoardRobotPumpNBT;
import buildcraft.robotics.boards.BoardRobotShovelman;
import buildcraft.robotics.boards.BoardRobotShovelmanNBT;
import buildcraft.robotics.boards.BoardRobotStripes;
import buildcraft.robotics.boards.BoardRobotStripesNBT;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionRobotFilterTool;
import buildcraft.robotics.statements.ActionRobotGotoStation;
import buildcraft.robotics.statements.ActionRobotWakeUp;
import buildcraft.robotics.statements.ActionRobotWorkInArea;
import buildcraft.robotics.statements.ActionStationAcceptFluids;
import buildcraft.robotics.statements.ActionStationAcceptItemsInv;
import buildcraft.robotics.statements.ActionStationAcceptItemsPipe;
import buildcraft.robotics.statements.ActionStationAllowCraft;
import buildcraft.robotics.statements.ActionStationForbidRobot;
import buildcraft.robotics.statements.ActionStationProvideFluids;
import buildcraft.robotics.statements.ActionStationProvideItems;
import buildcraft.robotics.statements.ActionStationRequestItems;
import buildcraft.robotics.statements.ActionStationRequestItemsMachine;
import buildcraft.robotics.statements.RobotsActionProvider;
import buildcraft.robotics.statements.RobotsTriggerProvider;
import buildcraft.robotics.statements.TriggerRobotSleep;
import buildcraft.silicon.ItemRedstoneChipset;

@Mod(name = "BuildCraft Robotics", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Robotics", dependencies = DefaultProps.DEPENDENCY_SILICON_TRANSPORT)
public class BuildCraftRobotics extends BuildCraftMod {
	@Mod.Instance("BuildCraft|Robotics")
	public static BuildCraftRobotics instance;

	public static ItemRedstoneBoard redstoneBoard;
	public static Item robotItem;

	public static ITriggerInternal triggerRobotSleep = new TriggerRobotSleep();

	public static IActionInternal actionRobotGotoStation = new ActionRobotGotoStation();
	public static IActionInternal actionRobotWakeUp = new ActionRobotWakeUp();
	public static IActionInternal actionRobotWorkInArea = new ActionRobotWorkInArea();
	public static IActionInternal actionRobotFilter = new ActionRobotFilter();
	public static IActionInternal actionRobotFilterTool = new ActionRobotFilterTool();
	public static IActionInternal actionRobotAllowCraft = new ActionStationAllowCraft();
	public static IActionInternal actionStationRequestItems = new ActionStationRequestItems();
	public static IActionInternal actionStationAcceptItems = new ActionStationAcceptItemsInv();
	public static IActionInternal actionStationProvideItems = new ActionStationProvideItems();
	public static IActionInternal actionStationAcceptFluids = new ActionStationAcceptFluids();
	public static IActionInternal actionStationProvideFluids = new ActionStationProvideFluids();
	public static IActionInternal actionStationForbidRobot = new ActionStationForbidRobot();
	public static IActionInternal actionStationDropInPipe = new ActionStationAcceptItemsPipe();
	public static IActionInternal actionStationMachineRequestItems = new ActionStationRequestItemsMachine();

	public static Achievement timeForSomeLogicAchievement;
	public static Achievement tinglyLaserAchievement;

	public static float chipsetCostMultiplier = 1.0F;

	public static List<String> blacklistedRobots;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		chipsetCostMultiplier = BuildCraftCore.mainConfiguration.getFloat("chipset.costMultiplier", "general", 1.0F, 0.001F, 1000.0F, "The multiplier for chipset recipe cost.");

		blacklistedRobots = new ArrayList<String>();
		blacklistedRobots.addAll(Arrays.asList(BuildCraftCore.mainConfiguration.get("general", "boards.blacklist", new String[]{}).getStringList()));

				BuildCraftCore.mainConfiguration.save();

		robotItem = new ItemRobot().setUnlocalizedName("robot");
		CoreProxy.proxy.registerItem(robotItem);

		redstoneBoard = new ItemRedstoneBoard();
		redstoneBoard.setUnlocalizedName("redstone_board");
		CoreProxy.proxy.registerItem(redstoneBoard);

		RedstoneBoardRegistry.instance = new ImplRedstoneBoardRegistry();

		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotPickerNBT.instance, 20);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotCarrierNBT.instance, 10);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotLumberjackNBT.instance, 10);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotHarvesterNBT.instance, 10);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotMinerNBT.instance, 10);

		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotFluidCarrierNBT.instance, 5);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotPlanterNBT.instance, 5);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotFarmerNBT.instance, 5);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotLeaveCutterNBT.instance, 5);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotButcherNBT.instance, 5);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotShovelmanNBT.instance, 5);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotCrafterNBT.instance, 5);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotDeliveryNBT.instance, 5);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotPumpNBT.instance, 5);

		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotKnightNBT.instance, 1);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotBomberNBT.instance, 1);

		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotBuilderNBT.instance, 0.5F);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotStripesNBT.instance, 0.5F);

		StatementManager.registerActionProvider(new RobotsActionProvider());
		StatementManager.registerTriggerProvider(new RobotsTriggerProvider());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		EntityRegistry.registerModEntity(EntityRobot.class, "bcRobot", EntityIds.ROBOT, instance, 50, 1, true);

		RobotManager.registryProvider = new RobotRegistryProvider();

		RobotManager.registerAIRobot(AIRobotMain.class, "aiRobotMain", "buildcraft.core.robots.AIRobotMain");
		RobotManager.registerAIRobot(BoardRobotBomber.class, "boardRobotBomber", "buildcraft.core.robots.boards.BoardRobotBomber");
		RobotManager.registerAIRobot(BoardRobotBuilder.class, "boardRobotBuilder", "buildcraft.core.robots.boards.BoardRobotBuilder");
		RobotManager.registerAIRobot(BoardRobotButcher.class, "boardRobotButcher", "buildcraft.core.robots.boards.BoardRobotButcher");
		RobotManager.registerAIRobot(BoardRobotCarrier.class, "boardRobotCarrier", "buildcraft.core.robots.boards.BoardRobotCarrier");
		RobotManager.registerAIRobot(BoardRobotCrafter.class, "boardRobotCrafter", "buildcraft.core.robots.boards.BoardRobotCrafter");
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
		RobotManager.registerAIRobot(AIRobotCraftAssemblyTable.class, "aiRobotCraftAssemblyTable", "buildcraft.core.robots.AIRobotCraftAssemblyTable");
		RobotManager.registerAIRobot(AIRobotCraftFurnace.class, "aiRobotCraftFurnace", "buildcraft.core.robots.AIRobotCraftFurnace");
		RobotManager.registerAIRobot(AIRobotCraftWorkbench.class, "aiRobotCraftWorkbench", "buildcraft.core.robots.AIRobotCraftWorkbench");
		RobotManager.registerAIRobot(AIRobotDeliverRequested.class, "aiRobotDeliverRequested", "buildcraft.core.robots.AIRobotDeliverRequested");
		RobotManager.registerAIRobot(AIRobotDisposeItems.class, "aiRobotDisposeItems", "buildcraft.core.robots.AIRobotDisposeItems");
		RobotManager.registerAIRobot(AIRobotFetchAndEquipItemStack.class, "aiRobotFetchAndEquipItemStack", "buildcraft.core.robots.AIRobotFetchAndEquipItemStack");
		RobotManager.registerAIRobot(AIRobotFetchItem.class, "aiRobotFetchItem", "buildcraft.core.robots.AIRobotFetchItem");
		RobotManager.registerAIRobot(AIRobotGoAndLinkToDock.class, "aiRobotGoAndLinkToDock", "buildcraft.core.robots.AIRobotGoAndLinkToDock");
		RobotManager.registerAIRobot(AIRobotGoto.class, "aiRobotGoto", "buildcraft.core.robots.AIRobotGoto");
		RobotManager.registerAIRobot(AIRobotGotoBlock.class, "aiRobotGotoBlock", "buildcraft.core.robots.AIRobotGotoBlock");
		RobotManager.registerAIRobot(AIRobotGotoSleep.class, "aiRobotGotoSleep", "buildcraft.core.robots.AIRobotGotoSleep");
		RobotManager.registerAIRobot(AIRobotGotoStation.class, "aiRobotGotoStation", "buildcraft.core.robots.AIRobotGotoStation");
		RobotManager.registerAIRobot(AIRobotGotoStationAndLoad.class, "aiRobotGotoStationAndLoad", "buildcraft.core.robots.AIRobotGotoStationAndLoad");
		RobotManager.registerAIRobot(AIRobotGotoStationAndLoadFluids.class, "aiRobotGotoStationAndLoadFluids", "buildcraft.core.robots.AIRobotGotoStationAndLoadFluids");
		RobotManager.registerAIRobot(AIRobotGotoStationAndUnload.class, "aiRobotGotoStationAndUnload", "buildcraft.core.robots.AIRobotGotoStationAndUnload");
		RobotManager.registerAIRobot(AIRobotGotoStationToLoad.class, "aiRobotGotoStationToLoad", "buildcraft.core.robots.AIRobotGotoStationToLoad");
		RobotManager.registerAIRobot(AIRobotGotoStationToLoadFluids.class, "aiRobotGotoStationToLoadFluids", "buildcraft.core.robots.AIRobotGotoStationToLoadFluids");
		RobotManager.registerAIRobot(AIRobotGotoStationToUnload.class, "aiRobotGotoStationToUnload", "buildcraft.core.robots.AIRobotGotoStationToUnload");
		RobotManager.registerAIRobot(AIRobotGotoStationToUnloadFluids.class, "aiRobotGotoStationToUnloadFluids", "buildcraft.core.robots.AIRobotGotoStationToUnloadFluids");
		RobotManager.registerAIRobot(AIRobotLoad.class, "aiRobotLoad", "buildcraft.core.robots.AIRobotLoad");
		RobotManager.registerAIRobot(AIRobotLoadFluids.class, "aiRobotLoadFluids", "buildcraft.core.robots.AIRobotLoadFluids");
		RobotManager.registerAIRobot(AIRobotPumpBlock.class, "aiRobotPumpBlock", "buildcraft.core.robots.AIRobotPumpBlock");
		RobotManager.registerAIRobot(AIRobotRecharge.class, "aiRobotRecharge", "buildcraft.core.robots.AIRobotRecharge");
		RobotManager.registerAIRobot(AIRobotSearchAndGotoStation.class, "aiRobotSearchAndGotoStation", "buildcraft.core.robots.AIRobotSearchAndGotoStation");
		RobotManager.registerAIRobot(AIRobotSearchBlock.class, "aiRobotSearchBlock", "buildcraft.core.robots.AIRobotSearchBlock");
		RobotManager.registerAIRobot(AIRobotSearchEntity.class, "aiRobotSearchEntity", "buildcraft.core.robots.AIRobotSearchEntity");
		RobotManager.registerAIRobot(AIRobotSearchRandomBlock.class, "aiRobotSearchRandomBlock", "buildcraft.core.robots.AIRobotSearchRandomBlock");
		RobotManager.registerAIRobot(AIRobotSearchRandomGroundBlock.class, "aiRobotSearchRandomGroundBlock", "buildcraft.core.robots.AIRobotSearchRandomGroundBlock");
		RobotManager.registerAIRobot(AIRobotSearchStackRequest.class, "aiRobotSearchStackRequest", "buildcraft.core.robots.AIRobotSearchStackRequest");
		RobotManager.registerAIRobot(AIRobotSearchStation.class, "aiRobotSearchStation", "buildcraft.core.robots.AIRobotSearchStation");
		RobotManager.registerAIRobot(AIRobotSleep.class, "aiRobotSleep", "buildcraft.core.robots.AIRobotSleep");
		RobotManager.registerAIRobot(AIRobotStraightMoveTo.class, "aiRobotStraightMoveTo", "buildcraft.core.robots.AIRobotStraightMoveTo");
		RobotManager.registerAIRobot(AIRobotUnload.class, "aiRobotUnload", "buildcraft.core.robots.AIRobotUnload");
		RobotManager.registerAIRobot(AIRobotUnloadFluids.class, "aiRobotUnloadFluids", "buildcraft.core.robots.AIRobotUnloadFluids");
		RobotManager.registerAIRobot(AIRobotUseToolOnBlock.class, "aiRobotUseToolOnBlock", "buildcraft.core.robots.AIRobotUseToolOnBlock");
		RobotManager.registerResourceId(ResourceIdAssemblyTable.class, "resourceIdAssemblyTable", "buildcraft.core.robots.ResourceIdAssemblyTable");
		RobotManager.registerResourceId(ResourceIdBlock.class, "resourceIdBlock", "buildcraft.core.robots.ResourceIdBlock");
		RobotManager.registerResourceId(ResourceIdRequest.class, "resourceIdRequest", "buildcraft.core.robots.ResourceIdRequest");

		RoboticsProxy.proxy.registerRenderers();
	}

	public static void loadRecipes() {
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(robotItem),
				"PPP",
				"PRP",
				"C C",
				'P', "ingotIron",
				'R', BuildCraftSilicon.redstoneCrystal,
				'C', ItemRedstoneChipset.Chipset.DIAMOND.getStack());


		CoreProxy.proxy.addCraftingRecipe(new ItemStack(redstoneBoard),
				"PPP",
				"PRP",
				"PPP",
				'R', "dustRedstone",
				'P', Items.paper);

		BuildcraftRecipeRegistry.programmingTable.addRecipe(new BoardProgrammingRecipe());
		BuildcraftRecipeRegistry.integrationTable.addRecipe(new RobotIntegrationRecipe("buildcraft:robotIntegration"));
	}

	@Mod.EventHandler
	public void processRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}

	@Mod.EventHandler
	public void remap(FMLMissingMappingsEvent event) {
		for (FMLMissingMappingsEvent.MissingMapping mapping: event.get()) {
			if (mapping.type == GameRegistry.Type.ITEM) {
				if (mapping.name.equals("BuildCraft|Silicon:robot")) {
					mapping.remap(robotItem);
				} else if (mapping.name.equals("BuildCraft|Silicon:redstone_board")) {
					mapping.remap(redstoneBoard);
				}
			}
		}
	}
}

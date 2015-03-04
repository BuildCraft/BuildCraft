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
import cpw.mods.fml.common.registry.GameRegistry;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.robotics.BoardProgrammingRecipe;
import buildcraft.robotics.ImplRedstoneBoardRegistry;
import buildcraft.robotics.ItemRedstoneBoard;
import buildcraft.robotics.ItemRobot;
import buildcraft.robotics.RobotIntegrationRecipe;
import buildcraft.robotics.RoboticsProxy;
import buildcraft.robotics.boards.BoardRobotBomberNBT;
import buildcraft.robotics.boards.BoardRobotBuilderNBT;
import buildcraft.robotics.boards.BoardRobotButcherNBT;
import buildcraft.robotics.boards.BoardRobotCarrierNBT;
import buildcraft.robotics.boards.BoardRobotCrafterNBT;
import buildcraft.robotics.boards.BoardRobotDeliveryNBT;
import buildcraft.robotics.boards.BoardRobotFarmerNBT;
import buildcraft.robotics.boards.BoardRobotFluidCarrierNBT;
import buildcraft.robotics.boards.BoardRobotHarvesterNBT;
import buildcraft.robotics.boards.BoardRobotKnightNBT;
import buildcraft.robotics.boards.BoardRobotLeaveCutterNBT;
import buildcraft.robotics.boards.BoardRobotLumberjackNBT;
import buildcraft.robotics.boards.BoardRobotMinerNBT;
import buildcraft.robotics.boards.BoardRobotPickerNBT;
import buildcraft.robotics.boards.BoardRobotPlanterNBT;
import buildcraft.robotics.boards.BoardRobotPumpNBT;
import buildcraft.robotics.boards.BoardRobotShovelmanNBT;
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

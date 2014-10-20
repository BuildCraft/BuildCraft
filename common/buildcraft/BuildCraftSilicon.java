/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import buildcraft.api.blueprints.SchematicRegistry;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.StatementManager;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.builders.schematics.SchematicRotateMeta;
import buildcraft.commander.BlockRequester;
import buildcraft.commander.BlockZonePlan;
import buildcraft.commander.TileRequester;
import buildcraft.commander.TileZonePlan;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.ItemRobot;
import buildcraft.core.Version;
import buildcraft.core.network.BuildCraftChannelHandler;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.robots.RobotIntegrationRecipe;
import buildcraft.core.robots.boards.BoardRobotBomberNBT;
import buildcraft.core.robots.boards.BoardRobotBuilderNBT;
import buildcraft.core.robots.boards.BoardRobotButcherNBT;
import buildcraft.core.robots.boards.BoardRobotCarrierNBT;
import buildcraft.core.robots.boards.BoardRobotCrafterNBT;
import buildcraft.core.robots.boards.BoardRobotDeliveryNBT;
import buildcraft.core.robots.boards.BoardRobotFarmerNBT;
import buildcraft.core.robots.boards.BoardRobotFluidCarrierNBT;
import buildcraft.core.robots.boards.BoardRobotHarvesterNBT;
import buildcraft.core.robots.boards.BoardRobotKnightNBT;
import buildcraft.core.robots.boards.BoardRobotLeaveCutterNBT;
import buildcraft.core.robots.boards.BoardRobotLumberjackNBT;
import buildcraft.core.robots.boards.BoardRobotMinerNBT;
import buildcraft.core.robots.boards.BoardRobotPickerNBT;
import buildcraft.core.robots.boards.BoardRobotPlanterNBT;
import buildcraft.core.robots.boards.BoardRobotPumpNBT;
import buildcraft.core.robots.boards.BoardRobotShovelmanNBT;
import buildcraft.core.science.TechnoRobot;
import buildcraft.core.science.TechnoSimpleItem;
import buildcraft.core.science.TechnoStatement;
import buildcraft.core.science.Tier;
import buildcraft.silicon.BlockLaser;
import buildcraft.silicon.BlockLaserTable;
import buildcraft.silicon.GuiHandler;
import buildcraft.silicon.ItemLaserTable;
import buildcraft.silicon.ItemRedstoneBoard;
import buildcraft.silicon.ItemRedstoneChipset;
import buildcraft.silicon.ItemRedstoneChipset.Chipset;
import buildcraft.silicon.SiliconProxy;
import buildcraft.silicon.TileAdvancedCraftingTable;
import buildcraft.silicon.TileAssemblyTable;
import buildcraft.silicon.TileIntegrationTable;
import buildcraft.silicon.TileLaser;
import buildcraft.silicon.boards.BoardRecipe;
import buildcraft.silicon.boards.ImplRedstoneBoardRegistry;
import buildcraft.silicon.network.PacketHandlerSilicon;
import buildcraft.silicon.statements.ActionRobotFilter;
import buildcraft.silicon.statements.ActionRobotGotoStation;
import buildcraft.silicon.statements.ActionRobotWakeUp;
import buildcraft.silicon.statements.ActionRobotWorkInArea;
import buildcraft.silicon.statements.ActionStationAcceptFluids;
import buildcraft.silicon.statements.ActionStationAcceptItemsInv;
import buildcraft.silicon.statements.ActionStationAcceptItemsPipe;
import buildcraft.silicon.statements.ActionStationAllowCraft;
import buildcraft.silicon.statements.ActionStationForbidRobot;
import buildcraft.silicon.statements.ActionStationProvideFluids;
import buildcraft.silicon.statements.ActionStationProvideItems;
import buildcraft.silicon.statements.ActionStationRequestItems;
import buildcraft.silicon.statements.ActionStationRequestItemsMachine;
import buildcraft.silicon.statements.RobotsActionProvider;
import buildcraft.silicon.statements.RobotsTriggerProvider;
import buildcraft.silicon.statements.TriggerRobotSleep;

@Mod(name = "BuildCraft Silicon", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Silicon", dependencies = DefaultProps.DEPENDENCY_TRANSPORT)
public class BuildCraftSilicon extends BuildCraftMod {

	@Mod.Instance("BuildCraft|Silicon")
	public static BuildCraftSilicon instance;

	public static ItemRedstoneChipset redstoneChipset;
	public static ItemRedstoneBoard redstoneBoard;
	public static BlockLaser laserBlock;
	public static BlockLaserTable assemblyTableBlock;
	public static BlockZonePlan zonePlanBlock;
	public static BlockRequester requesterBlock;
	public static Item redstoneCrystal;
	public static Item robotItem;

	public static ITrigger triggerRobotSleep = new TriggerRobotSleep();

	public static IAction actionRobotGotoStation = new ActionRobotGotoStation();
	public static IAction actionRobotWakeUp = new ActionRobotWakeUp();
	public static IAction actionRobotWorkInArea = new ActionRobotWorkInArea();
	public static IAction actionRobotFilter = new ActionRobotFilter();
	public static IAction actionRobotAllowCraft = new ActionStationAllowCraft();
	public static IAction actionStationRequestItems = new ActionStationRequestItems();
	public static IAction actionStationAcceptItems = new ActionStationAcceptItemsInv();
	public static IAction actionStationProvideItems = new ActionStationProvideItems();
	public static IAction actionStationAcceptFluids = new ActionStationAcceptFluids();
	public static IAction actionStationProvideFluids = new ActionStationProvideFluids();
	public static IAction actionStationForbidRobot = new ActionStationForbidRobot();
	public static IAction actionStationDropInPipe = new ActionStationAcceptItemsPipe();
	public static IAction actionStationMachineRequestItems = new ActionStationRequestItemsMachine();

	public static TechnoSimpleItem technoRedstoneBoard = new TechnoSimpleItem();
	public static TechnoSimpleItem technoLaserBlock = new TechnoSimpleItem();
	public static TechnoSimpleItem technoAssemblyTableBlock = new TechnoSimpleItem();
	public static TechnoSimpleItem technoAdvancedCraftingTableBlock = new TechnoSimpleItem();
	public static TechnoSimpleItem technoIntegrationTableBlock = new TechnoSimpleItem();
	public static TechnoSimpleItem technoZonePlanBlock = new TechnoSimpleItem();
	public static TechnoSimpleItem technoRedstoneCrystal = new TechnoSimpleItem();
	public static TechnoSimpleItem technoRobotItem = new TechnoSimpleItem();

	public static TechnoStatement technoTriggerRobotSleep = new TechnoStatement();
	public static TechnoStatement technoActionRobotGotoStation = new TechnoStatement();
	public static TechnoStatement technoActionRobotWakeUp = new TechnoStatement();
	public static TechnoStatement technoActionRobotWorkInArea = new TechnoStatement();
	public static TechnoStatement technoActionRobotFilter = new TechnoStatement();
	public static TechnoStatement technoActionStationRequestItems = new TechnoStatement();
	public static TechnoStatement technoActionStationForbidRobot = new TechnoStatement();
	public static TechnoStatement technoActionStationDropInPipe = new TechnoStatement();

	public static TechnoRobot technoRobotPicker = new TechnoRobot();
	public static TechnoRobot technoRobotCarrier = new TechnoRobot();
	public static TechnoRobot technoRobotLumberjack = new TechnoRobot();
	public static TechnoRobot technoRobotHarvester = new TechnoRobot();
	public static TechnoRobot technoRobotMiner = new TechnoRobot();
	public static TechnoRobot technoRobotPlanter = new TechnoRobot();
	public static TechnoRobot technoRobotFarmer = new TechnoRobot();
	public static TechnoRobot technoRobotLeaveCutter = new TechnoRobot();
	public static TechnoRobot technoRobotButcher = new TechnoRobot();
	public static TechnoRobot technoRobotShovelman = new TechnoRobot();
	public static TechnoRobot technoRobotKnight = new TechnoRobot();
	public static TechnoRobot technoRobotBomber = new TechnoRobot();
	public static TechnoRobot technoRobotBuilder = new TechnoRobot();

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		SchematicRegistry.declareBlueprintSupport("BuildCraft|Silicon");

		BuildCraftCore.mainConfiguration.save();

		laserBlock = new BlockLaser();
		laserBlock.setBlockName("laserBlock");
		CoreProxy.proxy.registerBlock(laserBlock);

		assemblyTableBlock = new BlockLaserTable();
		assemblyTableBlock.setBlockName("laserTableBlock");
		CoreProxy.proxy.registerBlock(assemblyTableBlock, ItemLaserTable.class);

		zonePlanBlock = new BlockZonePlan();
		zonePlanBlock.setBlockName("zonePlan");
		CoreProxy.proxy.registerBlock(zonePlanBlock);

		requesterBlock = new BlockRequester();
		requesterBlock.setBlockName("requester");
		CoreProxy.proxy.registerBlock(requesterBlock);

		redstoneChipset = new ItemRedstoneChipset();
		redstoneChipset.setUnlocalizedName("redstoneChipset");
		CoreProxy.proxy.registerItem(redstoneChipset);
		redstoneChipset.registerItemStacks();

		redstoneBoard = new ItemRedstoneBoard();
		redstoneBoard.setUnlocalizedName("redstone_board");
		CoreProxy.proxy.registerItem(redstoneBoard);

		redstoneCrystal = (new ItemBuildCraft()).setUnlocalizedName("redstoneCrystal");
		CoreProxy.proxy.registerItem(redstoneCrystal);
		OreDictionary.registerOre("redstoneCrystal", new ItemStack(redstoneCrystal));

		robotItem = new ItemRobot().setUnlocalizedName("robot");
		CoreProxy.proxy.registerItem(robotItem);

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

		StatementManager.registerActionProvider(new RobotsActionProvider());
		StatementManager.registerTriggerProvider(new RobotsTriggerProvider());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		channels = NetworkRegistry.INSTANCE
				.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-SILICON", new BuildCraftChannelHandler(), new PacketHandlerSilicon());

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
		CoreProxy.proxy.registerTileEntity(TileLaser.class, "net.minecraft.src.buildcraft.factory.TileLaser");
		CoreProxy.proxy.registerTileEntity(TileAssemblyTable.class,
				"net.minecraft.src.buildcraft.factory.TileAssemblyTable");
		CoreProxy.proxy.registerTileEntity(TileAdvancedCraftingTable.class,
				"net.minecraft.src.buildcraft.factory.TileAssemblyAdvancedWorkbench");
		CoreProxy.proxy.registerTileEntity(TileIntegrationTable.class,
				"net.minecraft.src.buildcraft.factory.TileIntegrationTable");
		CoreProxy.proxy.registerTileEntity(TileZonePlan.class, "net.minecraft.src.buildcraft.commander.TileZonePlan");
		CoreProxy.proxy.registerTileEntity(TileRequester.class, "net.minecraft.src.buildcraft.commander.TileRequester");

		SchematicRegistry.registerSchematicBlock(laserBlock, SchematicRotateMeta.class, new int[] {2, 5, 3, 4}, true);

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		SiliconProxy.proxy.registerRenderers();
	}

	@Mod.EventHandler
	public void loadTechnology(FMLPostInitializationEvent evt) {
		// Items and blocks

		technoLaserBlock.initialize(
				Tier.EmeraldGear,
				laserBlock,
				new ItemStack(BuildCraftCore.diamondGearItem /* emerald */, 5));

		technoAssemblyTableBlock.initialize(
				Tier.EmeraldGear,
				new ItemStack(assemblyTableBlock, 1, 0),
				new ItemStack(BuildCraftCore.diamondGearItem /* emerald */, 5));

		technoRedstoneCrystal.initialize(
				Tier.EmeraldGear,
				redstoneCrystal,
				new ItemStack(BuildCraftCore.diamondGearItem /* emerald */, 10));

		technoAdvancedCraftingTableBlock.initialize(
				Tier.RedstoneCrystalGear,
				new ItemStack(assemblyTableBlock, 1, 1),
				new ItemStack(BuildCraftCore.diamondGearItem /* emerald */, 5));

		technoIntegrationTableBlock.initialize(
				Tier.RedstoneCrystalGear,
				new ItemStack(assemblyTableBlock, 1, 2),
				new ItemStack(BuildCraftCore.diamondGearItem /* emerald */, 5));

		technoRedstoneBoard.initialize(
				Tier.DiamondChipset,
				redstoneBoard,
				new ItemStack(BuildCraftCore.diamondGearItem /* */, 5));

		technoRobotItem.initialize(
				Tier.DiamondChipset,
				robotItem,
				new ItemStack(BuildCraftCore.diamondGearItem /* */, 5));

		technoZonePlanBlock.initialize(
				Tier.RedstoneCrystalChipset,
				zonePlanBlock,
				new ItemStack(BuildCraftCore.diamondGearItem /* */, 5));

		// Statements

		technoTriggerRobotSleep.initialize(
				Tier.DiamondChipset,
				triggerRobotSleep,
				"",
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoActionRobotGotoStation.initialize(
				Tier.DiamondChipset,
				actionRobotGotoStation,
				"",
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoActionRobotWakeUp.initialize(
				Tier.DiamondChipset,
				actionRobotWakeUp,
				"",
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoActionRobotWorkInArea.initialize(
				Tier.EmeraldChipset,
				actionRobotWorkInArea,
				"",
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoActionRobotFilter.initialize(
				Tier.EmeraldChipset,
				actionRobotFilter,
				"",
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoActionStationRequestItems.initialize(
				Tier.DiamondChipset,
				actionStationRequestItems,
				"",
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoActionStationForbidRobot.initialize(
				Tier.EmeraldChipset,
				actionStationForbidRobot,
				"",
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoActionStationDropInPipe.initialize(
				Tier.DiamondChipset,
				actionStationDropInPipe,
				"",
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		// Robots

		technoRobotPicker.initialize(
				Tier.DiamondChipset,
				BoardRobotPickerNBT.instance,
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoRobotCarrier.initialize(
				Tier.DiamondChipset,
				BoardRobotCarrierNBT.instance,
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoRobotLumberjack.initialize(
				Tier.DiamondChipset,
				BoardRobotLumberjackNBT.instance,
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoRobotHarvester.initialize(
				Tier.DiamondChipset,
				BoardRobotHarvesterNBT.instance,
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoRobotMiner.initialize(
				Tier.DiamondChipset,
				BoardRobotMinerNBT.instance,
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoRobotPlanter.initialize(
				Tier.DiamondChipset,
				BoardRobotPlanterNBT.instance,
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoRobotFarmer.initialize(
				Tier.DiamondChipset,
				BoardRobotFarmerNBT.instance,
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoRobotLeaveCutter.initialize(
				Tier.DiamondChipset,
				BoardRobotLeaveCutterNBT.instance,
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoRobotButcher.initialize(
				Tier.DiamondChipset,
				BoardRobotButcherNBT.instance,
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoRobotShovelman.initialize(
				Tier.DiamondChipset,
				BoardRobotShovelmanNBT.instance,
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoRobotBuilder.initialize(
				Tier.EmeraldChipset,
				BoardRobotBuilderNBT.instance,
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoRobotKnight.initialize(
				Tier.EmeraldChipset,
				BoardRobotKnightNBT.instance,
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

		technoRobotBomber.initialize(
				Tier.EmeraldChipset,
				BoardRobotBomberNBT.instance,
				Chipset.RED.getStack(5),
				BuildCraftCore.technoRobotics);

	}

	public static void loadRecipes() {

		// TABLES
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(laserBlock),
				"ORR",
				"DDR",
				"ORR",
				'O', Blocks.obsidian,
				'R', Items.redstone,
				'D', Items.diamond);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 0),
				"ORO",
				"ODO",
				"OGO",
				'O', Blocks.obsidian,
				'R', Items.redstone,
				'D', Items.diamond,
				'G', BuildCraftCore.diamondGearItem);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 1),
				"OWO",
				"OCO",
				"ORO",
				'O', Blocks.obsidian,
				'W', Blocks.crafting_table,
				'C', Blocks.chest,
				'R', new ItemStack(redstoneChipset, 1, 0));

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 2),
				"ORO",
				"OCO",
				"OGO",
				'O', Blocks.obsidian,
				'R', Items.redstone,
				'C', new ItemStack(redstoneChipset, 1, 0),
				'G', BuildCraftCore.diamondGearItem);

		// CHIPSETS
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:redstoneChipset", 100000, Chipset.RED.getStack(),
				Items.redstone);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:ironChipset", 200000, Chipset.IRON.getStack(),
				Items.redstone, Items.iron_ingot);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:goldChipset", 400000, Chipset.GOLD.getStack(),
				Items.redstone, Items.gold_ingot);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:diamondChipset", 800000,
				Chipset.DIAMOND.getStack(), Items.redstone, Items.diamond);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:pulsatingChipset", 400000,
				Chipset.PULSATING.getStack(2), Items.redstone, Items.ender_pearl);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:quartzChipset", 600000, Chipset.QUARTZ.getStack(),
				Items.redstone, Items.quartz);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:compChipset", 600000, Chipset.COMP.getStack(),
				Items.redstone, Items.comparator);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:emeraldChipset", 1200000,
				Chipset.EMERALD.getStack(), Items.redstone, Items.emerald);

		// ROBOTS AND BOARDS
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:redstoneCrystal", 10000000, new ItemStack(
				redstoneCrystal), new ItemStack(
				Blocks.redstone_block));

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(redstoneBoard),
				"PPP",
				"PRP",
				"PPP",
				'R', Items.redstone,
				'P', Items.paper);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(robotItem),
				"PPP",
				"PRP",
				"C C",
				'P', Items.iron_ingot,
				'R', redstoneCrystal,
				'C', Chipset.DIAMOND.getStack());

		BuildcraftRecipeRegistry.assemblyTable.addRecipe(new BoardRecipe("buildcraft:redstoneBoard"));
		BuildcraftRecipeRegistry.integrationTable.addRecipe(new RobotIntegrationRecipe("buildcraft:robotIntegration"));
	}

	@Mod.EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}

	@Mod.EventHandler
	public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileLaser.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileAssemblyTable.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileAdvancedCraftingTable.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileIntegrationTable.class.getCanonicalName());
	}

	@Mod.EventHandler
	public void remap(FMLMissingMappingsEvent event) {
		for (FMLMissingMappingsEvent.MissingMapping mapping: event.get()) {
			if (mapping.name.equals("BuildCraft|Silicon:null")) {
				if (mapping.type == GameRegistry.Type.ITEM) {
					mapping.remap(Item.getItemFromBlock(laserBlock));
				} else {
					mapping.remap(laserBlock);
				}
			}
		}
	}
}

/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.blueprints.SchematicRegistry;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.gates.ActionParameterItemStack;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.StatementManager;
import buildcraft.api.gates.TriggerParameterItemStack;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.transport.PipeWire;
import buildcraft.builders.schematics.SchematicRotateMeta;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.ItemRobot;
import buildcraft.core.Version;
import buildcraft.core.network.BuildCraftChannelHandler;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.robots.RobotIntegrationRecipe;
import buildcraft.core.robots.boards.BoardRobotBomberNBT;
import buildcraft.core.robots.boards.BoardRobotCarrierNBT;
import buildcraft.core.robots.boards.BoardRobotKnightNBT;
import buildcraft.core.robots.boards.BoardRobotLeaveCutterNBT;
import buildcraft.core.robots.boards.BoardRobotLumberjackNBT;
import buildcraft.core.robots.boards.BoardRobotMinerNBT;
import buildcraft.core.robots.boards.BoardRobotPickerNBT;
import buildcraft.core.robots.boards.BoardRobotPlanterNBT;
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
import buildcraft.silicon.recipes.AdvancedFacadeRecipe;
import buildcraft.silicon.recipes.GateExpansionRecipe;
import buildcraft.silicon.recipes.GateLogicSwapRecipe;
import buildcraft.silicon.statements.ActionRobotGotoStation;
import buildcraft.silicon.statements.ActionRobotWakeUp;
import buildcraft.silicon.statements.ActionRobotWorkInArea;
import buildcraft.silicon.statements.ActionStationForbidRobot;
import buildcraft.silicon.statements.ActionStationProvideItems;
import buildcraft.silicon.statements.ActionStationRequestItems;
import buildcraft.silicon.statements.RobotsActionProvider;
import buildcraft.silicon.statements.RobotsTriggerProvider;
import buildcraft.silicon.statements.TriggerRobotSleep;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.GateExpansionPulsar;
import buildcraft.transport.gates.GateExpansionRedstoneFader;
import buildcraft.transport.gates.GateExpansionTimer;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.triggers.ActionParameterSignal;
import buildcraft.transport.triggers.TriggerParameterSignal;

@Mod(name = "BuildCraft Silicon", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Silicon", dependencies = DefaultProps.DEPENDENCY_TRANSPORT)
public class BuildCraftSilicon extends BuildCraftMod {

	public static ItemRedstoneChipset redstoneChipset;
	public static ItemRedstoneBoard redstoneBoard;
	public static BlockLaser laserBlock;
	public static BlockLaserTable assemblyTableBlock;
	@Mod.Instance("BuildCraft|Silicon")
	public static BuildCraftSilicon instance;

	public static Item redstoneCrystal;
	public static Item robotItem;

	public static IAction actionRobotGotoStation = new ActionRobotGotoStation();
	public static IAction actionRobotWakeUp = new ActionRobotWakeUp();
	public static IAction actionRobotWorkInArea = new ActionRobotWorkInArea();
	public static IAction actionStationRequestItems = new ActionStationRequestItems();
	public static IAction actionStationProvideItems = new ActionStationProvideItems();
	public static IAction actionStationForbidRobot = new ActionStationForbidRobot();

	public static ITrigger triggerRobotSleep = new TriggerRobotSleep();

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		SchematicRegistry.declareBlueprintSupport("BuildCraft|Silicon");

		BuildCraftCore.mainConfiguration.save();

		laserBlock = new BlockLaser();
		laserBlock.setBlockName("laserBlock");
		CoreProxy.proxy.registerBlock(laserBlock);

		assemblyTableBlock = new BlockLaserTable();
		CoreProxy.proxy.registerBlock(assemblyTableBlock, ItemLaserTable.class);

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
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotMinerNBT.instance, 10);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotPlanterNBT.instance, 5);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotLeaveCutterNBT.instance, 5);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotKnightNBT.instance, 1);
		RedstoneBoardRegistry.instance.registerBoardClass(BoardRobotBomberNBT.instance, 1);

		StatementManager.registerActionProvider(new RobotsActionProvider());
		StatementManager.registerTriggerProvider(new RobotsTriggerProvider());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		channels = NetworkRegistry.INSTANCE.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-SILICON", new BuildCraftChannelHandler(), new PacketHandlerSilicon());

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
		CoreProxy.proxy.registerTileEntity(TileLaser.class, "net.minecraft.src.buildcraft.factory.TileLaser");
		CoreProxy.proxy.registerTileEntity(TileAssemblyTable.class, "net.minecraft.src.buildcraft.factory.TileAssemblyTable");
		CoreProxy.proxy.registerTileEntity(TileAdvancedCraftingTable.class, "net.minecraft.src.buildcraft.factory.TileAssemblyAdvancedWorkbench");
		CoreProxy.proxy.registerTileEntity(TileIntegrationTable.class, "net.minecraft.src.buildcraft.factory.TileIntegrationTable");

		SchematicRegistry.registerSchematicBlock(laserBlock, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		SiliconProxy.proxy.registerRenderers();

		StatementManager.registerParameterClass("buildcraft:stackTrigger", TriggerParameterItemStack.class);
		StatementManager.registerParameterClass("buildcraft:pipeWireTrigger", TriggerParameterSignal.class);
		StatementManager.registerParameterClass("buildcraft:stackAction", ActionParameterItemStack.class);
		StatementManager.registerParameterClass("buildcraft:pipeWireAction", ActionParameterSignal.class);
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

		// PIPE WIRE
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:redWire", 500, PipeWire.RED.getStack(8),
				OreDictionary.getOres("dyeRed"), Items.redstone, Items.iron_ingot);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:blueWire", 500, PipeWire.BLUE.getStack(8),
				OreDictionary.getOres("dyeBlue"), Items.redstone, Items.iron_ingot);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:greenWire", 500, PipeWire.GREEN.getStack(8),
				OreDictionary.getOres("dyeGreen"), Items.redstone, Items.iron_ingot);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:yellowWire", 500, PipeWire.YELLOW.getStack(8),
				OreDictionary.getOres("dyeYellow"), Items.redstone, Items.iron_ingot);

		// CHIPSETS
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:redstoneChipset", 10000, Chipset.RED.getStack(),
				Items.redstone);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:ironChipset", 20000, Chipset.IRON.getStack(),
				Items.redstone, Items.iron_ingot);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:goldChipset", 40000, Chipset.GOLD.getStack(),
				Items.redstone, Items.gold_ingot);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:diamondChipset", 80000,
				Chipset.DIAMOND.getStack(), Items.redstone, Items.diamond);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:pulsatingChipset", 40000,
				Chipset.PULSATING.getStack(2), Items.redstone, Items.ender_pearl);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:quartzChipset", 60000, Chipset.QUARTZ.getStack(),
				Items.redstone, Items.quartz);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:compChipset", 60000, Chipset.COMP.getStack(),
				Items.redstone, Items.comparator);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:emeraldChipset", 120000,
				Chipset.EMERALD.getStack(), Items.redstone, Items.emerald);

		// GATES
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:simpleGate", 10000,
				ItemGate.makeGateItem(GateMaterial.REDSTONE, GateLogic.AND), Chipset.RED.getStack(),
				PipeWire.RED.getStack());

		addGateRecipe("Iron", 20000, GateMaterial.IRON, Chipset.IRON, PipeWire.RED, PipeWire.BLUE);
		addGateRecipe("Gold", 40000, GateMaterial.GOLD, Chipset.GOLD, PipeWire.RED, PipeWire.BLUE, PipeWire.GREEN);
		addGateRecipe("Diamond", 80000, GateMaterial.DIAMOND, Chipset.DIAMOND, PipeWire.RED, PipeWire.BLUE,
				PipeWire.GREEN, PipeWire.YELLOW);
		addGateRecipe("Emerald", 120000, GateMaterial.EMERALD, Chipset.DIAMOND, PipeWire.RED, PipeWire.BLUE,
				PipeWire.GREEN, PipeWire.YELLOW);

		// ROBOTS AND BOARDS
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:redstoneCrystal", 1000000, new ItemStack(
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

		// REVERSAL RECIPE
		BuildcraftRecipeRegistry.integrationTable.addRecipe(new GateLogicSwapRecipe("buildcraft:gateSwap"));

		// EXPANSIONS
		BuildcraftRecipeRegistry.integrationTable.addRecipe(new GateExpansionRecipe("buildcraft:expansionPulsar",
				GateExpansionPulsar.INSTANCE, Chipset.PULSATING.getStack()));
		BuildcraftRecipeRegistry.integrationTable.addRecipe(new GateExpansionRecipe("buildcraft:expansionQuartz",
				GateExpansionTimer.INSTANCE, Chipset.QUARTZ.getStack()));
		BuildcraftRecipeRegistry.integrationTable.addRecipe(new GateExpansionRecipe("buildcraft:expansionComp",
				GateExpansionRedstoneFader.INSTANCE, Chipset.COMP.getStack()));

		// FACADE
		BuildcraftRecipeRegistry.integrationTable.addRecipe(new AdvancedFacadeRecipe("buildcraft:advancedFacade"));
	}

	private static void addGateRecipe(String materialName, double energyCost, GateMaterial material, Chipset chipset,
			PipeWire... pipeWire) {
		List<ItemStack> temp = new ArrayList<ItemStack>();
		temp.add(chipset.getStack());
		for (PipeWire wire : pipeWire) {
			temp.add(wire.getStack());
		}
		Object[] inputs = temp.toArray();
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:andGate" + materialName, energyCost,
				ItemGate.makeGateItem(material, GateLogic.AND), inputs);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:orGate" + materialName, energyCost,
				ItemGate.makeGateItem(material, GateLogic.OR), inputs);
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
}

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
import java.util.Arrays;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.builders.schematics.SchematicRotateMeta;
import buildcraft.commander.BlockRequester;
import buildcraft.commander.BlockZonePlan;
import buildcraft.commander.TileRequester;
import buildcraft.commander.TileZonePlan;
import buildcraft.compat.CompatHooks;
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
import buildcraft.silicon.TileChargingTable;
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

	public static ITriggerInternal triggerRobotSleep = new TriggerRobotSleep();

	public static IActionInternal actionRobotGotoStation = new ActionRobotGotoStation();
	public static IActionInternal actionRobotWakeUp = new ActionRobotWakeUp();
	public static IActionInternal actionRobotWorkInArea = new ActionRobotWorkInArea();
	public static IActionInternal actionRobotFilter = new ActionRobotFilter();
	public static IActionInternal actionRobotAllowCraft = new ActionStationAllowCraft();
	public static IActionInternal actionStationRequestItems = new ActionStationRequestItems();
	public static IActionInternal actionStationAcceptItems = new ActionStationAcceptItemsInv();
	public static IActionInternal actionStationProvideItems = new ActionStationProvideItems();
	public static IActionInternal actionStationAcceptFluids = new ActionStationAcceptFluids();
	public static IActionInternal actionStationProvideFluids = new ActionStationProvideFluids();
	public static IActionInternal actionStationForbidRobot = new ActionStationForbidRobot();
	public static IActionInternal actionStationDropInPipe = new ActionStationAcceptItemsPipe();
	public static IActionInternal actionStationMachineRequestItems = new ActionStationRequestItemsMachine();

	public static float chipsetCostMultiplier = 1.0F;

	public static List<String> blacklistedRobots;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		if (isDisabled(evt.getModMetadata().modId)) {
			disabled = true;
			return;
		}
		chipsetCostMultiplier = BuildCraftCore.mainConfiguration.getFloat("chipset.costMultiplier", Configuration.CATEGORY_GENERAL, 1.0F, 0.001F, 1000.0F, "The multiplier for chipset recipe cost.");

		blacklistedRobots = new ArrayList<String>();
		blacklistedRobots.addAll(Arrays.asList(BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "boards.blacklist", new String[]{}).getStringList()));

				BuildCraftCore.mainConfiguration.save();

		laserBlock = (BlockLaser) CompatHooks.INSTANCE.getBlock(BlockLaser.class);
		laserBlock.setBlockName("laserBlock");
		CoreProxy.proxy.registerBlock(laserBlock);

		assemblyTableBlock = (BlockLaserTable) CompatHooks.INSTANCE.getBlock(BlockLaserTable.class);
		assemblyTableBlock.setBlockName("laserTableBlock");
		CoreProxy.proxy.registerBlock(assemblyTableBlock, ItemLaserTable.class);

		zonePlanBlock = (BlockZonePlan) CompatHooks.INSTANCE.getBlock(BlockZonePlan.class);
		zonePlanBlock.setBlockName("zonePlan");
		CoreProxy.proxy.registerBlock(zonePlanBlock);

		requesterBlock = (BlockRequester) CompatHooks.INSTANCE.getBlock(BlockRequester.class);
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
		if (disabled) {
			return;
		}
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
        CoreProxy.proxy.registerTileEntity(TileChargingTable.class,
                "net.minecraft.src.buildcraft.factory.TileChargingTable");
		CoreProxy.proxy.registerTileEntity(TileZonePlan.class, "net.minecraft.src.buildcraft.commander.TileZonePlan");
		CoreProxy.proxy.registerTileEntity(TileRequester.class, "net.minecraft.src.buildcraft.commander.TileRequester");

		BuilderAPI.schematicRegistry.registerSchematicBlock(laserBlock, SchematicRotateMeta.class, new int[] {2, 5, 3, 4}, true);

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		SiliconProxy.proxy.registerRenderers();
	}

	public static void loadRecipes() {

		// TABLES
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(laserBlock),
				"ORR",
				"DDR",
				"ORR",
				'O', Blocks.obsidian,
				'R', "dustRedstone",
				'D', "gemDiamond");

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 0),
				"ORO",
				"ODO",
				"OGO",
				'O', Blocks.obsidian,
				'R', "dustRedstone",
				'D', "gemDiamond",
				'G', "gearDiamond");

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
				'R', "dustRedstone",
				'C', new ItemStack(redstoneChipset, 1, 0),
				'G', "gearDiamond");

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(assemblyTableBlock, 1, 3),
				"ORO",
				"OCO",
				"OGO",
				'O', Blocks.obsidian,
				'R', "dustRedstone",
				'C', new ItemStack(redstoneChipset, 1, 0),
				'G', "gearGold");

		// COMMANDER BLOCKS
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(zonePlanBlock, 1, 0),
				"IRI",
				"GMG",
				"IDI",
				'M', Items.map,
				'R', "dustRedstone",
				'G', "gearGold",
				'D', "gearDiamond",
				'I', "ingotIron");
		
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(requesterBlock, 1, 0),
				"IPI",
				"GCG",
				"IRI",
				'C', Blocks.chest,
				'R', "dustRedstone",
				'P', Blocks.piston,
				'G', "gearIron",
				'I', "ingotIron");
		
		// CHIPSETS
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:redstoneChipset", Math.round(100000 * chipsetCostMultiplier), Chipset.RED.getStack(),
				"dustRedstone");
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:ironChipset", Math.round(200000 * chipsetCostMultiplier), Chipset.IRON.getStack(),
				"dustRedstone", "ingotIron");
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:goldChipset", Math.round(400000 * chipsetCostMultiplier), Chipset.GOLD.getStack(),
				"dustRedstone", "ingotGold");
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:diamondChipset", Math.round(800000 * chipsetCostMultiplier),
				Chipset.DIAMOND.getStack(), "dustRedstone", "gemDiamond");
        BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:emeraldChipset", Math.round(1200000 * chipsetCostMultiplier),
                Chipset.EMERALD.getStack(), "dustRedstone", "gemEmerald");
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:pulsatingChipset", Math.round(400000 * chipsetCostMultiplier),
				Chipset.PULSATING.getStack(2), "dustRedstone", Items.ender_pearl);
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:quartzChipset", Math.round(600000 * chipsetCostMultiplier), Chipset.QUARTZ.getStack(),
				"dustRedstone", "gemQuartz");
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:compChipset", Math.round(600000 * chipsetCostMultiplier), Chipset.COMP.getStack(),
				"dustRedstone", Items.comparator);

		// ROBOTS AND BOARDS
		BuildcraftRecipeRegistry.assemblyTable.addRecipe("buildcraft:redstoneCrystal", 10000000, new ItemStack(
				redstoneCrystal), new ItemStack(
				Blocks.redstone_block));

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(redstoneBoard),
				"PPP",
				"PRP",
				"PPP",
				'R', "dustRedstone",
				'P', Items.paper);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(robotItem),
				"PPP",
				"PRP",
				"C C",
				'P', "ingotIron",
				'R', redstoneCrystal,
				'C', Chipset.DIAMOND.getStack());

		BuildcraftRecipeRegistry.assemblyTable.addRecipe(new BoardRecipe("buildcraft:redstoneBoard"));
		BuildcraftRecipeRegistry.integrationTable.addRecipe(new RobotIntegrationRecipe("buildcraft:robotIntegration"));
	}

	@Mod.EventHandler
	public void processRequests(FMLInterModComms.IMCEvent event) {
		if (disabled) {
			return;
		}
		InterModComms.processIMC(event);
	}

	@Mod.EventHandler
	public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
		if (disabled) {
			return;
		}
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
		if (disabled) {
			return;
		}
		for (FMLMissingMappingsEvent.MissingMapping mapping: event.get()) {
			if (mapping.name.equals("BuildCraft|Silicon:null")) {
				if (mapping.type == GameRegistry.Type.ITEM) {
					mapping.remap(Item.getItemFromBlock(assemblyTableBlock));
				} else {
					mapping.remap(assemblyTableBlock);
				}
			}
		}
	}
}

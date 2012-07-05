/**
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src;

import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;

import net.minecraft.src.buildcraft.api.BptBlock;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockBed;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockCustomStack;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockDelegate;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockDirt;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockDoor;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockIgnore;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockIgnoreMeta;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockInventory;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockLever;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockLiquid;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockPiston;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockPumpkin;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockRedstoneRepeater;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockRotateInventory;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockRotateMeta;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockSign;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockStairs;
import net.minecraft.src.buildcraft.api.bptblocks.BptBlockWallSide;
import net.minecraft.src.buildcraft.api.filler.FillerManager;
import net.minecraft.src.buildcraft.builders.BlockArchitect;
import net.minecraft.src.buildcraft.builders.BlockBlueprintLibrary;
import net.minecraft.src.buildcraft.builders.BlockBuilder;
import net.minecraft.src.buildcraft.builders.BlockFiller;
import net.minecraft.src.buildcraft.builders.BlockMarker;
import net.minecraft.src.buildcraft.builders.BlockPathMarker;
import net.minecraft.src.buildcraft.builders.BptBlockFiller;
import net.minecraft.src.buildcraft.builders.BuildersSaveManager;
import net.minecraft.src.buildcraft.builders.FillerFillAll;
import net.minecraft.src.buildcraft.builders.FillerFillPyramid;
import net.minecraft.src.buildcraft.builders.FillerFillStairs;
import net.minecraft.src.buildcraft.builders.FillerFillWalls;
import net.minecraft.src.buildcraft.builders.FillerFlattener;
import net.minecraft.src.buildcraft.builders.FillerRegistry;
import net.minecraft.src.buildcraft.builders.FillerRemover;
import net.minecraft.src.buildcraft.builders.GuiHandler;
import net.minecraft.src.buildcraft.builders.IBuilderHook;
import net.minecraft.src.buildcraft.builders.ItemBptBluePrint;
import net.minecraft.src.buildcraft.builders.ItemBptTemplate;
import net.minecraft.src.buildcraft.builders.TileArchitect;
import net.minecraft.src.buildcraft.builders.TileBlueprintLibrary;
import net.minecraft.src.buildcraft.builders.TileBuilder;
import net.minecraft.src.buildcraft.builders.TileFiller;
import net.minecraft.src.buildcraft.builders.TileMarker;
import net.minecraft.src.buildcraft.builders.TilePathMarker;
import net.minecraft.src.buildcraft.core.BptPlayerIndex;
import net.minecraft.src.buildcraft.core.BptRootIndex;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.forge.Configuration;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.Property;

public class BuildCraftBuilders {

	public static final int LIBRARY_PAGE_SIZE = 12;

	public static final int MAX_BLUEPRINTS_NAME_SIZE = 88;

	public static BlockMarker markerBlock;
	public static BlockPathMarker pathMarkerBlock;
	public static BlockFiller fillerBlock;
	public static BlockBuilder builderBlock;
	public static BlockArchitect architectBlock;
	public static BlockBlueprintLibrary libraryBlock;
	public static ItemBptTemplate templateItem;
	public static ItemBptBluePrint blueprintItem;
	public static boolean fillerDestroy;

	private static boolean initialized = false;

	private static BptRootIndex rootBptIndex;

	public static TreeMap<String, BptPlayerIndex> playerLibrary = new TreeMap<String, BptPlayerIndex>();

	private static LinkedList<IBuilderHook> hooks = new LinkedList<IBuilderHook>();

	public static void load() {

		// Create filler registry
		FillerManager.registry = new FillerRegistry();

		// Register gui handler
		MinecraftForge.setGuiHandler(mod_BuildCraftBuilders.instance, new GuiHandler());
		
		// Register save handler
		MinecraftForge.registerSaveHandler(new BuildersSaveManager());
	}

	public static void initialize() {
		if (initialized)
			return;
		else
			initialized = true;

		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeGears();

		Property templateItemId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("templateItem.id",
				Configuration.CATEGORY_ITEM, DefaultProps.TEMPLATE_ITEM_ID);
		Property blueprintItemId = BuildCraftCore.mainConfiguration.getOrCreateIntProperty("blueprintItem.id",
				Configuration.CATEGORY_ITEM, DefaultProps.BLUEPRINT_ITEM_ID);
		Property markerId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("marker.id", DefaultProps.MARKER_ID);
		Property pathMarkerId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("pathMarker.id",
				DefaultProps.PATH_MARKER_ID);
		Property fillerId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("filler.id", DefaultProps.FILLER_ID);
		Property builderId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("builder.id", DefaultProps.BUILDER_ID);
		Property architectId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("architect.id",
				DefaultProps.ARCHITECT_ID);
		Property libraryId = BuildCraftCore.mainConfiguration.getOrCreateBlockIdProperty("blueprintLibrary.id",
				DefaultProps.BLUEPRINT_LIBRARY_ID);

		Property fillerDestroyProp = BuildCraftCore.mainConfiguration.getOrCreateBooleanProperty("filler.destroy",
				Configuration.CATEGORY_GENERAL, DefaultProps.FILLER_DESTROY);
		fillerDestroyProp.comment = "If true, Filler will destroy blocks instead of breaking them.";
		fillerDestroy = Boolean.parseBoolean(fillerDestroyProp.value);

		BuildCraftCore.mainConfiguration.save();

		templateItem = new ItemBptTemplate(Integer.parseInt(templateItemId.value));
		templateItem.setItemName("templateItem");
		CoreProxy.addName(templateItem, "Template");

		blueprintItem = new ItemBptBluePrint(Integer.parseInt(blueprintItemId.value));
		blueprintItem.setItemName("blueprintItem");
		CoreProxy.addName(blueprintItem, "Blueprint");

		markerBlock = new BlockMarker(Integer.parseInt(markerId.value));
		CoreProxy.registerBlock(markerBlock.setBlockName("markerBlock"));
		CoreProxy.addName(markerBlock, "Land Mark");

		pathMarkerBlock = new BlockPathMarker(Integer.parseInt(pathMarkerId.value));
		CoreProxy.registerBlock(pathMarkerBlock.setBlockName("pathMarkerBlock"));
		CoreProxy.addName(pathMarkerBlock, "Path Mark");

		fillerBlock = new BlockFiller(Integer.parseInt(fillerId.value));
		CoreProxy.registerBlock(fillerBlock.setBlockName("fillerBlock"));
		CoreProxy.addName(fillerBlock, "Filler");

		builderBlock = new BlockBuilder(Integer.parseInt(builderId.value));
		CoreProxy.registerBlock(builderBlock.setBlockName("builderBlock"));
		CoreProxy.addName(builderBlock, "Builder");

		architectBlock = new BlockArchitect(Integer.parseInt(architectId.value));
		CoreProxy.registerBlock(architectBlock.setBlockName("architectBlock"));
		CoreProxy.addName(architectBlock, "Architect Table");

		libraryBlock = new BlockBlueprintLibrary(Integer.parseInt(libraryId.value));
		CoreProxy.registerBlock(libraryBlock.setBlockName("libraryBlock"));
		CoreProxy.addName(libraryBlock, "Blueprint Library");

		CoreProxy.registerTileEntity(TileMarker.class, "Marker");
		CoreProxy.registerTileEntity(TileFiller.class, "Filler");
		CoreProxy.registerTileEntity(TileBuilder.class, "net.minecraft.src.builders.TileBuilder");
		CoreProxy.registerTileEntity(TileArchitect.class, "net.minecraft.src.builders.TileTemplate");
		CoreProxy.registerTileEntity(TilePathMarker.class, "net.minecraft.src.builders.TilePathMarker");
		CoreProxy.registerTileEntity(TileBlueprintLibrary.class, "net.minecraft.src.builders.TileBlueprintLibrary");

		// / INIT FILLER PATTERNS
		FillerManager.registry.addRecipe(new FillerFillAll(), new Object[] { "bbb", "bbb", "bbb", Character.valueOf('b'),
				new ItemStack(Block.brick, 1) });

		FillerManager.registry.addRecipe(new FillerFlattener(), new Object[] { "   ", "ggg", "bbb", Character.valueOf('g'),
				Block.glass, Character.valueOf('b'), Block.brick });

		FillerManager.registry.addRecipe(new FillerRemover(), new Object[] { "ggg", "ggg", "ggg", Character.valueOf('g'),
				Block.glass });

		FillerManager.registry.addRecipe(new FillerFillWalls(), new Object[] { "bbb", "b b", "bbb", Character.valueOf('b'),
				Block.brick });

		FillerManager.registry.addRecipe(new FillerFillPyramid(), new Object[] { "   ", " b ", "bbb", Character.valueOf('b'),
				Block.brick });

		FillerManager.registry.addRecipe(new FillerFillStairs(), new Object[] { "  b", " bb", "bbb", Character.valueOf('b'),
				Block.brick });

		BuildCraftCore.mainConfiguration.save();

		// public static final Block music;
		// public static final Block cloth;
		// public static final Block tilledField;
		// public static final BlockPortal portal;
		// public static final Block trapdoor;

		// STANDARD BLOCKS

		new BptBlock(0); // default bpt block

		new BptBlockIgnore(Block.snow.blockID);
		new BptBlockIgnore(Block.tallGrass.blockID);
		new BptBlockIgnore(Block.ice.blockID);
		new BptBlockIgnore(Block.pistonExtension.blockID);

		new BptBlockDirt(Block.dirt.blockID);
		new BptBlockDirt(Block.grass.blockID);
		new BptBlockDirt(Block.tilledField.blockID);

		new BptBlockDelegate(Block.torchRedstoneIdle.blockID, Block.torchRedstoneActive.blockID);
		new BptBlockDelegate(Block.stoneOvenActive.blockID, Block.stoneOvenIdle.blockID);
		new BptBlockDelegate(Block.pistonMoving.blockID, Block.pistonBase.blockID);

		new BptBlockWallSide(Block.torchWood.blockID);
		new BptBlockWallSide(Block.torchRedstoneActive.blockID);

		new BptBlockRotateMeta(Block.ladder.blockID, new int[] { 2, 5, 3, 4 }, true);
		new BptBlockRotateMeta(Block.fenceGate.blockID, new int[] { 0, 1, 2, 3 }, true);

		new BptBlockRotateInventory(Block.stoneOvenIdle.blockID, new int[] { 2, 5, 3, 4 }, true);
		new BptBlockRotateInventory(Block.chest.blockID, new int[] { 2, 5, 3, 4 }, true);
		new BptBlockRotateInventory(Block.lockedChest.blockID, new int[] { 2, 5, 3, 4 }, true);
		new BptBlockRotateInventory(Block.dispenser.blockID, new int[] { 2, 5, 3, 4 }, true);

		new BptBlockInventory(Block.brewingStand.blockID);

		new BptBlockRotateMeta(Block.vine.blockID, new int[] { 1, 4, 8, 2 }, false);
		new BptBlockRotateMeta(Block.trapdoor.blockID, new int[] { 0, 1, 2, 3 }, false);

		new BptBlockLever(Block.button.blockID);
		new BptBlockLever(Block.lever.blockID);

		new BptBlockCustomStack(Block.stone.blockID, new ItemStack(Block.stone));
		new BptBlockCustomStack(Block.redstoneWire.blockID, new ItemStack(Item.redstone));
		new BptBlockCustomStack(Block.stairDouble.blockID, new ItemStack(Block.stairSingle, 2));
		new BptBlockCustomStack(Block.cake.blockID, new ItemStack(Item.cake));
		new BptBlockCustomStack(Block.crops.blockID, new ItemStack(Item.seeds));
		new BptBlockCustomStack(Block.pumpkinStem.blockID, new ItemStack(Item.pumpkinSeeds));
		new BptBlockCustomStack(Block.melonStem.blockID, new ItemStack(Item.melonSeeds));
		new BptBlockCustomStack(Block.glowStone.blockID, new ItemStack(Block.glowStone));

		new BptBlockRedstoneRepeater(Block.redstoneRepeaterActive.blockID);
		new BptBlockRedstoneRepeater(Block.redstoneRepeaterIdle.blockID);

		new BptBlockLiquid(Block.waterStill.blockID, new ItemStack(Item.bucketWater));
		new BptBlockLiquid(Block.waterMoving.blockID, new ItemStack(Item.bucketWater));
		new BptBlockLiquid(Block.lavaStill.blockID, new ItemStack(Item.bucketLava));
		new BptBlockLiquid(Block.lavaMoving.blockID, new ItemStack(Item.bucketLava));

		new BptBlockIgnoreMeta(Block.rail.blockID);
		new BptBlockIgnoreMeta(Block.railPowered.blockID);
		new BptBlockIgnoreMeta(Block.railDetector.blockID);
		new BptBlockIgnoreMeta(Block.thinGlass.blockID);

		new BptBlockPiston(Block.pistonBase.blockID);
		new BptBlockPiston(Block.pistonStickyBase.blockID);

		new BptBlockPumpkin(Block.pumpkinLantern.blockID);

		new BptBlockStairs(Block.stairCompactCobblestone.blockID);
		new BptBlockStairs(Block.stairCompactPlanks.blockID);
		new BptBlockStairs(Block.stairsNetherBrick.blockID);
		new BptBlockStairs(Block.stairsBrick.blockID);
		new BptBlockStairs(Block.stairsStoneBrickSmooth.blockID);

		new BptBlockDoor(Block.doorWood.blockID, new ItemStack(Item.doorWood));
		new BptBlockDoor(Block.doorSteel.blockID, new ItemStack(Item.doorSteel));

		new BptBlockBed(Block.bed.blockID);

		new BptBlockSign(Block.signWall.blockID, true);
		new BptBlockSign(Block.signPost.blockID, false);

		// BUILDCRAFT BLOCKS

		new BptBlockRotateInventory(architectBlock.blockID, new int[] { 2, 5, 3, 4 }, true);
		new BptBlockRotateInventory(builderBlock.blockID, new int[] { 2, 5, 3, 4 }, true);

		new BptBlockInventory(libraryBlock.blockID);

		new BptBlockWallSide(markerBlock.blockID);
		new BptBlockWallSide(pathMarkerBlock.blockID);
		new BptBlockFiller(fillerBlock.blockID);

		if (BuildCraftCore.loadDefaultRecipes)
			loadRecipes();
	}

	public static void loadRecipes() {
		CraftingManager craftingmanager = CraftingManager.getInstance();

		craftingmanager.addRecipe(new ItemStack(templateItem, 1), new Object[] { "ppp", "pip", "ppp", Character.valueOf('i'),
				new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('p'), Item.paper });

		craftingmanager.addRecipe(new ItemStack(blueprintItem, 1), new Object[] { "ppp", "pip", "ppp", Character.valueOf('i'),
				new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('p'), Item.paper });

		craftingmanager.addRecipe(new ItemStack(markerBlock, 1), new Object[] { "l ", "r ", Character.valueOf('l'),
				new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('r'), Block.torchRedstoneActive });

		craftingmanager.addRecipe(new ItemStack(pathMarkerBlock, 1), new Object[] { "l ", "r ", Character.valueOf('l'),
				new ItemStack(Item.dyePowder, 1, 2), Character.valueOf('r'), Block.torchRedstoneActive });

		craftingmanager.addRecipe(new ItemStack(fillerBlock, 1), new Object[] { "btb", "ycy", "gCg", Character.valueOf('b'),
				new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('t'), markerBlock, Character.valueOf('y'),
				new ItemStack(Item.dyePowder, 1, 11), Character.valueOf('c'), Block.workbench, Character.valueOf('g'),
				BuildCraftCore.goldGearItem, Character.valueOf('C'), Block.chest });

		craftingmanager.addRecipe(new ItemStack(builderBlock, 1), new Object[] { "btb", "ycy", "gCg", Character.valueOf('b'),
				new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('t'), markerBlock, Character.valueOf('y'),
				new ItemStack(Item.dyePowder, 1, 11), Character.valueOf('c'), Block.workbench, Character.valueOf('g'),
				BuildCraftCore.diamondGearItem, Character.valueOf('C'), Block.chest });

		craftingmanager.addRecipe(new ItemStack(architectBlock, 1), new Object[] { "btb", "ycy", "gCg", Character.valueOf('b'),
				new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('t'), markerBlock, Character.valueOf('y'),
				new ItemStack(Item.dyePowder, 1, 11), Character.valueOf('c'), Block.workbench, Character.valueOf('g'),
				BuildCraftCore.diamondGearItem, Character.valueOf('C'), new ItemStack(templateItem, 1) });

		craftingmanager.addRecipe(new ItemStack(libraryBlock, 1), new Object[] { "bbb", "bBb", "bbb", Character.valueOf('b'),
				new ItemStack(blueprintItem), Character.valueOf('B'), Block.bookShelf });
	}

	public static BptPlayerIndex getPlayerIndex(String name) {
		BptRootIndex rootIndex = getBptRootIndex();

		if (!playerLibrary.containsKey(name))
			try {
				playerLibrary.put(name, new BptPlayerIndex(name + ".list", rootIndex));
			} catch (IOException e) {
				e.printStackTrace();
			}

		return playerLibrary.get(name);
	}

	public static BptRootIndex getBptRootIndex() {
		if (rootBptIndex == null)
			try {
				rootBptIndex = new BptRootIndex("index.txt");
				rootBptIndex.loadIndex();

				for (IBuilderHook hook : hooks)
					hook.rootIndexInitialized(rootBptIndex);

				rootBptIndex.importNewFiles();

			} catch (IOException e) {
				e.printStackTrace();
			}

		return rootBptIndex;
	}

	public static void addHook(IBuilderHook hook) {
		if (!hooks.contains(hook))
			hooks.add(hook);
	}

}

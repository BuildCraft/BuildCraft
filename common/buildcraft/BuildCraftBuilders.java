/**
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft;

import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.bptblocks.BptBlockBed;
import buildcraft.api.bptblocks.BptBlockCustomStack;
import buildcraft.api.bptblocks.BptBlockDelegate;
import buildcraft.api.bptblocks.BptBlockDirt;
import buildcraft.api.bptblocks.BptBlockDoor;
import buildcraft.api.bptblocks.BptBlockIgnore;
import buildcraft.api.bptblocks.BptBlockIgnoreMeta;
import buildcraft.api.bptblocks.BptBlockInventory;
import buildcraft.api.bptblocks.BptBlockLever;
import buildcraft.api.bptblocks.BptBlockLiquid;
import buildcraft.api.bptblocks.BptBlockPiston;
import buildcraft.api.bptblocks.BptBlockPumpkin;
import buildcraft.api.bptblocks.BptBlockRedstoneRepeater;
import buildcraft.api.bptblocks.BptBlockRotateInventory;
import buildcraft.api.bptblocks.BptBlockRotateMeta;
import buildcraft.api.bptblocks.BptBlockSign;
import buildcraft.api.bptblocks.BptBlockStairs;
import buildcraft.api.bptblocks.BptBlockWallSide;
import buildcraft.api.filler.FillerManager;
import buildcraft.builders.BlockArchitect;
import buildcraft.builders.BlockBlueprintLibrary;
import buildcraft.builders.BlockBuilder;
import buildcraft.builders.BlockFiller;
import buildcraft.builders.BlockMarker;
import buildcraft.builders.BlockPathMarker;
import buildcraft.builders.BptBlockFiller;
import buildcraft.builders.EventHandlerBuilders;
import buildcraft.builders.FillerFillAll;
import buildcraft.builders.FillerFillPyramid;
import buildcraft.builders.FillerFillStairs;
import buildcraft.builders.FillerFillWalls;
import buildcraft.builders.FillerFlattener;
import buildcraft.builders.FillerRegistry;
import buildcraft.builders.FillerRemover;
import buildcraft.builders.GuiHandler;
import buildcraft.builders.IBuilderHook;
import buildcraft.builders.ItemBptBluePrint;
import buildcraft.builders.ItemBptTemplate;
import buildcraft.builders.TileArchitect;
import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.builders.TileBuilder;
import buildcraft.builders.TileFiller;
import buildcraft.builders.TileMarker;
import buildcraft.builders.TilePathMarker;
import buildcraft.builders.network.PacketHandlerBuilders;
import buildcraft.core.DefaultProps;
import buildcraft.core.Version;
import buildcraft.core.blueprints.BptPlayerIndex;
import buildcraft.core.blueprints.BptRootIndex;
import buildcraft.core.proxy.CoreProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(name = "BuildCraft Builders", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Builders", dependencies = DefaultProps.DEPENDENCY_CORE)
@NetworkMod(channels = { DefaultProps.NET_CHANNEL_NAME }, packetHandler = PacketHandlerBuilders.class, clientSideRequired = true, serverSideRequired = true)
public class BuildCraftBuilders {

	public static final int LIBRARY_PAGE_SIZE = 12;

	public static final int MAX_BLUEPRINTS_NAME_SIZE = 14;

	public static BlockMarker markerBlock;
	public static BlockPathMarker pathMarkerBlock;
	public static BlockFiller fillerBlock;
	public static BlockBuilder builderBlock;
	public static BlockArchitect architectBlock;
	public static BlockBlueprintLibrary libraryBlock;
	public static ItemBptTemplate templateItem;
	public static ItemBptBluePrint blueprintItem;
	public static boolean fillerDestroy;

	private static BptRootIndex rootBptIndex;

	public static TreeMap<String, BptPlayerIndex> playerLibrary = new TreeMap<String, BptPlayerIndex>();

	private static LinkedList<IBuilderHook> hooks = new LinkedList<IBuilderHook>();

	@Instance("BuildCraft|Builders")
	public static BuildCraftBuilders instance;

	@Init
	public void load(FMLInitializationEvent evt) {
		// Create filler registry
		FillerManager.registry = new FillerRegistry();

		// Register gui handler
		NetworkRegistry.instance().registerGuiHandler(instance, new GuiHandler());

		// Register save handler
		MinecraftForge.EVENT_BUS.register(new EventHandlerBuilders());

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

		new BptBlockLever(Block.woodenButton.blockID);
		new BptBlockLever(Block.stoneButton.blockID);
		new BptBlockLever(Block.lever.blockID);

		new BptBlockCustomStack(Block.stone.blockID, new ItemStack(Block.stone));
		new BptBlockCustomStack(Block.redstoneWire.blockID, new ItemStack(Item.redstone));
		// FIXME: Not sure what this has become
		// new BptBlockCustomStack(Block.stairDouble.blockID, new ItemStack(Block.stairSingle, 2));
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

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

	}

	@PreInit
	public void initialize(FMLPreInitializationEvent evt) {
		Property templateItemId = BuildCraftCore.mainConfiguration.getItem("templateItem.id", DefaultProps.TEMPLATE_ITEM_ID);
		Property blueprintItemId = BuildCraftCore.mainConfiguration.getItem("blueprintItem.id", DefaultProps.BLUEPRINT_ITEM_ID);
		Property markerId = BuildCraftCore.mainConfiguration.getBlock("marker.id", DefaultProps.MARKER_ID);
		Property pathMarkerId = BuildCraftCore.mainConfiguration.getBlock("pathMarker.id", DefaultProps.PATH_MARKER_ID);
		Property fillerId = BuildCraftCore.mainConfiguration.getBlock("filler.id", DefaultProps.FILLER_ID);
		Property builderId = BuildCraftCore.mainConfiguration.getBlock("builder.id", DefaultProps.BUILDER_ID);
		Property architectId = BuildCraftCore.mainConfiguration.getBlock("architect.id", DefaultProps.ARCHITECT_ID);
		Property libraryId = BuildCraftCore.mainConfiguration.getBlock("blueprintLibrary.id", DefaultProps.BLUEPRINT_LIBRARY_ID);

		Property fillerDestroyProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "filler.destroy", DefaultProps.FILLER_DESTROY);
		fillerDestroyProp.comment = "If true, Filler will destroy blocks instead of breaking them.";
		fillerDestroy = fillerDestroyProp.getBoolean(DefaultProps.FILLER_DESTROY);

		templateItem = new ItemBptTemplate(Integer.parseInt(templateItemId.value));
		templateItem.setItemName("templateItem");
		LanguageRegistry.addName(templateItem, "Template");

		blueprintItem = new ItemBptBluePrint(Integer.parseInt(blueprintItemId.value));
		blueprintItem.setItemName("blueprintItem");
		LanguageRegistry.addName(blueprintItem, "Blueprint");

		markerBlock = new BlockMarker(Integer.parseInt(markerId.value));
		CoreProxy.proxy.registerBlock(markerBlock.setBlockName("markerBlock"));
		CoreProxy.proxy.addName(markerBlock, "Land Mark");

		pathMarkerBlock = new BlockPathMarker(Integer.parseInt(pathMarkerId.value));
		CoreProxy.proxy.registerBlock(pathMarkerBlock.setBlockName("pathMarkerBlock"));
		CoreProxy.proxy.addName(pathMarkerBlock, "Path Mark");

		fillerBlock = new BlockFiller(Integer.parseInt(fillerId.value));
		CoreProxy.proxy.registerBlock(fillerBlock.setBlockName("fillerBlock"));
		CoreProxy.proxy.addName(fillerBlock, "Filler");

		builderBlock = new BlockBuilder(Integer.parseInt(builderId.value));
		CoreProxy.proxy.registerBlock(builderBlock.setBlockName("builderBlock"));
		CoreProxy.proxy.addName(builderBlock, "Builder");

		architectBlock = new BlockArchitect(Integer.parseInt(architectId.value));
		CoreProxy.proxy.registerBlock(architectBlock.setBlockName("architectBlock"));
		CoreProxy.proxy.addName(architectBlock, "Architect Table");

		libraryBlock = new BlockBlueprintLibrary(Integer.parseInt(libraryId.value));
		CoreProxy.proxy.registerBlock(libraryBlock.setBlockName("libraryBlock"));
		CoreProxy.proxy.addName(libraryBlock, "Blueprint Library");

		GameRegistry.registerTileEntity(TileMarker.class, "Marker");
		GameRegistry.registerTileEntity(TileFiller.class, "Filler");
		GameRegistry.registerTileEntity(TileBuilder.class, "net.minecraft.src.builders.TileBuilder");
		GameRegistry.registerTileEntity(TileArchitect.class, "net.minecraft.src.builders.TileTemplate");
		GameRegistry.registerTileEntity(TilePathMarker.class, "net.minecraft.src.builders.TilePathMarker");
		GameRegistry.registerTileEntity(TileBlueprintLibrary.class, "net.minecraft.src.builders.TileBlueprintLibrary");

		BuildCraftCore.mainConfiguration.save();

		// public static final Block music;
		// public static final Block cloth;
		// public static final Block tilledField;
		// public static final BlockPortal portal;
		// public static final Block trapdoor;

		// STANDARD BLOCKS
	}

	public static void loadRecipes() {

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(templateItem, 1), new Object[] { "ppp", "pip", "ppp", Character.valueOf('i'),
				new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('p'), Item.paper });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(blueprintItem, 1), new Object[] { "ppp", "pip", "ppp", Character.valueOf('i'),
				new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('p'), Item.paper });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(markerBlock, 1), new Object[] { "l ", "r ", Character.valueOf('l'),
				new ItemStack(Item.dyePowder, 1, 4), Character.valueOf('r'), Block.torchRedstoneActive });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(pathMarkerBlock, 1), new Object[] { "l ", "r ", Character.valueOf('l'),
				new ItemStack(Item.dyePowder, 1, 2), Character.valueOf('r'), Block.torchRedstoneActive });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(fillerBlock, 1), new Object[] { "btb", "ycy", "gCg", Character.valueOf('b'),
				new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('t'), markerBlock, Character.valueOf('y'), new ItemStack(Item.dyePowder, 1, 11),
				Character.valueOf('c'), Block.workbench, Character.valueOf('g'), BuildCraftCore.goldGearItem, Character.valueOf('C'), Block.chest });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(builderBlock, 1), new Object[] { "btb", "ycy", "gCg", Character.valueOf('b'),
				new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('t'), markerBlock, Character.valueOf('y'), new ItemStack(Item.dyePowder, 1, 11),
				Character.valueOf('c'), Block.workbench, Character.valueOf('g'), BuildCraftCore.diamondGearItem, Character.valueOf('C'), Block.chest });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(architectBlock, 1), new Object[] { "btb", "ycy", "gCg", Character.valueOf('b'),
				new ItemStack(Item.dyePowder, 1, 0), Character.valueOf('t'), markerBlock, Character.valueOf('y'), new ItemStack(Item.dyePowder, 1, 11),
				Character.valueOf('c'), Block.workbench, Character.valueOf('g'), BuildCraftCore.diamondGearItem, Character.valueOf('C'),
				new ItemStack(templateItem, 1) });

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(libraryBlock, 1), new Object[] { "bbb", "bBb", "bbb", Character.valueOf('b'),
				new ItemStack(blueprintItem), Character.valueOf('B'), Block.bookShelf });
		// / INIT FILLER PATTERNS
		FillerManager.registry.addRecipe(new FillerFillAll(), new Object[] { "bbb", "bbb", "bbb", Character.valueOf('b'), new ItemStack(Block.brick, 1) });

		FillerManager.registry.addRecipe(new FillerFlattener(), new Object[] { "   ", "ggg", "bbb", Character.valueOf('g'), Block.glass,
				Character.valueOf('b'), Block.brick });

		FillerManager.registry.addRecipe(new FillerRemover(), new Object[] { "ggg", "ggg", "ggg", Character.valueOf('g'), Block.glass });

		FillerManager.registry.addRecipe(new FillerFillWalls(), new Object[] { "bbb", "b b", "bbb", Character.valueOf('b'), Block.brick });

		FillerManager.registry.addRecipe(new FillerFillPyramid(), new Object[] { "   ", " b ", "bbb", Character.valueOf('b'), Block.brick });

		FillerManager.registry.addRecipe(new FillerFillStairs(), new Object[] { "  b", " bb", "bbb", Character.valueOf('b'), Block.brick });
	}

	public static BptPlayerIndex getPlayerIndex(String name) {
		BptRootIndex rootIndex = getBptRootIndex();

		if (!playerLibrary.containsKey(name)) {
			try {
				playerLibrary.put(name, new BptPlayerIndex(name + ".list", rootIndex));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return playerLibrary.get(name);
	}

	public static BptRootIndex getBptRootIndex() {
		if (rootBptIndex == null) {
			try {
				rootBptIndex = new BptRootIndex("index.txt");
				rootBptIndex.loadIndex();

				for (IBuilderHook hook : hooks) {
					hook.rootIndexInitialized(rootBptIndex);
				}

				rootBptIndex.importNewFiles();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return rootBptIndex;
	}

	public static ItemStack getBptItemStack(int id, int damage, String name) {
		ItemStack stack = new ItemStack(id, 1, damage);
		NBTTagCompound nbt = new NBTTagCompound();
		if (name != null && !"".equals(name)) {
			nbt.setString("BptName", name);
			stack.setTagCompound(nbt);
		}
		return stack;
	}

	public static void addHook(IBuilderHook hook) {
		if (!hooks.contains(hook)) {
			hooks.add(hook);
		}
	}

	@Mod.ServerStopping
	public void ServerStop(FMLServerStoppingEvent event) {
		TilePathMarker.clearAvailableMarkersList();
	}

}

/**
 * Copyright (c) SpaceToad, 2011-2012 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.bptblocks.BptBlockBed;
import buildcraft.api.bptblocks.BptBlockCustomStack;
import buildcraft.api.bptblocks.BptBlockDelegate;
import buildcraft.api.bptblocks.BptBlockDirt;
import buildcraft.api.bptblocks.BptBlockDoor;
import buildcraft.api.bptblocks.BptBlockFluid;
import buildcraft.api.bptblocks.BptBlockIgnore;
import buildcraft.api.bptblocks.BptBlockIgnoreMeta;
import buildcraft.api.bptblocks.BptBlockInventory;
import buildcraft.api.bptblocks.BptBlockLever;
import buildcraft.api.bptblocks.BptBlockPiston;
import buildcraft.api.bptblocks.BptBlockPumpkin;
import buildcraft.api.bptblocks.BptBlockRedstoneRepeater;
import buildcraft.api.bptblocks.BptBlockRotateInventory;
import buildcraft.api.bptblocks.BptBlockRotateMeta;
import buildcraft.api.bptblocks.BptBlockSign;
import buildcraft.api.bptblocks.BptBlockStairs;
import buildcraft.api.bptblocks.BptBlockWallSide;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.gates.ActionManager;
import buildcraft.builders.BlockArchitect;
import buildcraft.builders.BlockBlueprintLibrary;
import buildcraft.builders.BlockBuilder;
import buildcraft.builders.BlockFiller;
import buildcraft.builders.BlockMarker;
import buildcraft.builders.BlockPathMarker;
import buildcraft.builders.EventHandlerBuilders;
import buildcraft.builders.filler.pattern.PatternFill;
import buildcraft.builders.filler.pattern.PatternPyramid;
import buildcraft.builders.filler.pattern.PatternStairs;
import buildcraft.builders.filler.pattern.PatternBox;
import buildcraft.builders.filler.pattern.PatternFlatten;
import buildcraft.builders.filler.pattern.PatternHorizon;
import buildcraft.builders.filler.FillerRegistry;
import buildcraft.builders.filler.pattern.PatternClear;
import buildcraft.builders.GuiHandler;
import buildcraft.builders.IBuilderHook;
import buildcraft.builders.ItemBlueprintStandard;
import buildcraft.builders.ItemBlueprintTemplate;
import buildcraft.builders.TileArchitect;
import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.builders.TileBuilder;
import buildcraft.builders.TileFiller;
import buildcraft.builders.TileMarker;
import buildcraft.builders.TilePathMarker;
import buildcraft.builders.filler.pattern.FillerPattern;
import buildcraft.builders.network.PacketHandlerBuilders;
import buildcraft.builders.triggers.ActionFiller;
import buildcraft.builders.triggers.BuildersActionProvider;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.blueprints.BptPlayerIndex;
import buildcraft.core.blueprints.BptRootIndex;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BCLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeMap;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;

@Mod(name = "BuildCraft Builders", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Builders", dependencies = DefaultProps.DEPENDENCY_CORE)
@NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandlerBuilders.class, clientSideRequired = true, serverSideRequired = true)
public class BuildCraftBuilders {

	public static final int LIBRARY_PAGE_SIZE = 12;
	public static final int MAX_BLUEPRINTS_NAME_SIZE = 14;
	public static BlockMarker markerBlock;
	public static BlockPathMarker pathMarkerBlock;
	public static BlockFiller fillerBlock;
	public static BlockBuilder builderBlock;
	public static BlockArchitect architectBlock;
	public static BlockBlueprintLibrary libraryBlock;
	public static ItemBlueprintTemplate templateItem;
	public static ItemBlueprintStandard blueprintItem;
	public static boolean fillerDestroy;
	public static int fillerLifespanTough;
	public static int fillerLifespanNormal;
	public static ActionFiller[] fillerActions;
	private static BptRootIndex rootBptIndex;
	public static TreeMap<String, BptPlayerIndex> playerLibrary = new TreeMap<String, BptPlayerIndex>();
	private static LinkedList<IBuilderHook> hooks = new LinkedList<IBuilderHook>();
	@Instance("BuildCraft|Builders")
	public static BuildCraftBuilders instance;

	@EventHandler
	public void init(FMLInitializationEvent evt) {
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
		new BptBlockDelegate(Block.furnaceBurning.blockID, Block.furnaceIdle.blockID);
		new BptBlockDelegate(Block.pistonMoving.blockID, Block.pistonBase.blockID);

		new BptBlockWallSide(Block.torchWood.blockID);
		new BptBlockWallSide(Block.torchRedstoneActive.blockID);

		new BptBlockRotateMeta(Block.ladder.blockID, new int[]{2, 5, 3, 4}, true);
		new BptBlockRotateMeta(Block.fenceGate.blockID, new int[]{0, 1, 2, 3}, true);

		new BptBlockRotateInventory(Block.furnaceIdle.blockID, new int[]{2, 5, 3, 4}, true);
		new BptBlockRotateInventory(Block.chest.blockID, new int[]{2, 5, 3, 4}, true);
		new BptBlockRotateInventory(Block.lockedChest.blockID, new int[]{2, 5, 3, 4}, true);
		new BptBlockRotateInventory(Block.dispenser.blockID, new int[]{2, 5, 3, 4}, true);

		new BptBlockInventory(Block.brewingStand.blockID);

		new BptBlockRotateMeta(Block.vine.blockID, new int[]{1, 4, 8, 2}, false);
		new BptBlockRotateMeta(Block.trapdoor.blockID, new int[]{0, 1, 2, 3}, false);

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

		new BptBlockFluid(Block.waterStill.blockID, new ItemStack(Item.bucketWater));
		new BptBlockFluid(Block.waterMoving.blockID, new ItemStack(Item.bucketWater));
		new BptBlockFluid(Block.lavaStill.blockID, new ItemStack(Item.bucketLava));
		new BptBlockFluid(Block.lavaMoving.blockID, new ItemStack(Item.bucketLava));

		new BptBlockIgnoreMeta(Block.rail.blockID);
		new BptBlockIgnoreMeta(Block.railPowered.blockID);
		new BptBlockIgnoreMeta(Block.railDetector.blockID);
		new BptBlockIgnoreMeta(Block.thinGlass.blockID);

		new BptBlockPiston(Block.pistonBase.blockID);
		new BptBlockPiston(Block.pistonStickyBase.blockID);

		new BptBlockPumpkin(Block.pumpkinLantern.blockID);

		new BptBlockStairs(Block.stairsCobblestone.blockID);
		new BptBlockStairs(Block.stairsWoodOak.blockID);
		new BptBlockStairs(Block.stairsNetherBrick.blockID);
		new BptBlockStairs(Block.stairsBrick.blockID);
		new BptBlockStairs(Block.stairsStoneBrick.blockID);

		new BptBlockDoor(Block.doorWood.blockID, new ItemStack(Item.doorWood));
		new BptBlockDoor(Block.doorIron.blockID, new ItemStack(Item.doorIron));

		new BptBlockBed(Block.bed.blockID);

		new BptBlockSign(Block.signWall.blockID, true);
		new BptBlockSign(Block.signPost.blockID, false);

		// BUILDCRAFT BLOCKS

		new BptBlockRotateInventory(architectBlock.blockID, new int[]{2, 5, 3, 4}, true);
		new BptBlockRotateInventory(builderBlock.blockID, new int[]{2, 5, 3, 4}, true);

		new BptBlockInventory(libraryBlock.blockID);

		new BptBlockWallSide(markerBlock.blockID);
		new BptBlockWallSide(pathMarkerBlock.blockID);

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
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

		Property fillerLifespanToughProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "filler.lifespan.tough", DefaultProps.FILLER_LIFESPAN_TOUGH);
		fillerLifespanToughProp.comment = "Lifespan in ticks of items dropped by the filler from 'tough' blocks (those that can't be broken by hand)";
		fillerLifespanTough = fillerLifespanToughProp.getInt(DefaultProps.FILLER_LIFESPAN_TOUGH);

		Property fillerLifespanNormalProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "filler.lifespan.other", DefaultProps.FILLER_LIFESPAN_NORMAL);
		fillerLifespanNormalProp.comment = "Lifespan in ticks of items dropped by the filler from non-tough blocks (those that can be broken by hand)";
		fillerLifespanNormal = fillerLifespanNormalProp.getInt(DefaultProps.FILLER_LIFESPAN_NORMAL);

		templateItem = new ItemBlueprintTemplate(templateItemId.getInt());
		templateItem.setUnlocalizedName("templateItem");
		LanguageRegistry.addName(templateItem, "Template");
		CoreProxy.proxy.registerItem(templateItem);

		blueprintItem = new ItemBlueprintStandard(blueprintItemId.getInt());
		blueprintItem.setUnlocalizedName("blueprintItem");
		LanguageRegistry.addName(blueprintItem, "Blueprint");
		CoreProxy.proxy.registerItem(blueprintItem);

		markerBlock = new BlockMarker(markerId.getInt());
		CoreProxy.proxy.registerBlock(markerBlock.setUnlocalizedName("markerBlock"));
		CoreProxy.proxy.addName(markerBlock, "Land Mark");

		pathMarkerBlock = new BlockPathMarker(pathMarkerId.getInt());
		CoreProxy.proxy.registerBlock(pathMarkerBlock.setUnlocalizedName("pathMarkerBlock"));
		CoreProxy.proxy.addName(pathMarkerBlock, "Path Mark");

		fillerBlock = new BlockFiller(fillerId.getInt());
		CoreProxy.proxy.registerBlock(fillerBlock.setUnlocalizedName("fillerBlock"));
		CoreProxy.proxy.addName(fillerBlock, "Filler");

		builderBlock = new BlockBuilder(builderId.getInt());
		CoreProxy.proxy.registerBlock(builderBlock.setUnlocalizedName("builderBlock"));
		CoreProxy.proxy.addName(builderBlock, "Builder");

		architectBlock = new BlockArchitect(architectId.getInt());
		CoreProxy.proxy.registerBlock(architectBlock.setUnlocalizedName("architectBlock"));
		CoreProxy.proxy.addName(architectBlock, "Architect Table");

		libraryBlock = new BlockBlueprintLibrary(libraryId.getInt());
		CoreProxy.proxy.registerBlock(libraryBlock.setUnlocalizedName("libraryBlock"));
		CoreProxy.proxy.addName(libraryBlock, "Blueprint Library");

		GameRegistry.registerTileEntity(TileMarker.class, "Marker");
		GameRegistry.registerTileEntity(TileFiller.class, "Filler");
		GameRegistry.registerTileEntity(TileBuilder.class, "net.minecraft.src.builders.TileBuilder");
		GameRegistry.registerTileEntity(TileArchitect.class, "net.minecraft.src.builders.TileTemplate");
		GameRegistry.registerTileEntity(TilePathMarker.class, "net.minecraft.src.builders.TilePathMarker");
		GameRegistry.registerTileEntity(TileBlueprintLibrary.class, "net.minecraft.src.builders.TileBlueprintLibrary");

		if (BuildCraftCore.mainConfiguration.hasChanged()) {
			BuildCraftCore.mainConfiguration.save();
		}

		MinecraftForge.EVENT_BUS.register(this);

		// Create filler registry
		try {
			FillerManager.registry = new FillerRegistry();

			// INIT FILLER PATTERNS
			FillerManager.registry.addPattern(PatternFill.INSTANCE);
			FillerManager.registry.addPattern(new PatternFlatten());
			FillerManager.registry.addPattern(new PatternHorizon());
			FillerManager.registry.addPattern(new PatternClear());
			FillerManager.registry.addPattern(new PatternBox());
			FillerManager.registry.addPattern(new PatternPyramid());
			FillerManager.registry.addPattern(new PatternStairs());
		} catch (Error error) {
			BCLog.logErrorAPI("Buildcraft", error, IFillerPattern.class);
			throw error;
		}

		ActionManager.registerActionProvider(new BuildersActionProvider());
	}

	public static void loadRecipes() {

//		CoreProxy.proxy.addCraftingRecipe(new ItemStack(templateItem, 1), new Object[]{"ppp", "pip", "ppp", 'i',
//			new ItemStack(Item.dyePowder, 1, 0), 'p', Item.paper});

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(blueprintItem, 1), new Object[]{"ppp", "pip", "ppp", 'i',
			new ItemStack(Item.dyePowder, 1, 4), 'p', Item.paper});

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(markerBlock, 1), new Object[]{"l ", "r ", 'l',
			new ItemStack(Item.dyePowder, 1, 4), 'r', Block.torchRedstoneActive});

//		CoreProxy.proxy.addCraftingRecipe(new ItemStack(pathMarkerBlock, 1), new Object[]{"l ", "r ", 'l',
//			new ItemStack(Item.dyePowder, 1, 2), 'r', Block.torchRedstoneActive});

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(fillerBlock, 1), new Object[]{"btb", "ycy", "gCg", 'b',
			new ItemStack(Item.dyePowder, 1, 0), 't', markerBlock, 'y', new ItemStack(Item.dyePowder, 1, 11),
			'c', Block.workbench, 'g', BuildCraftCore.goldGearItem, 'C', Block.chest});

//		CoreProxy.proxy.addCraftingRecipe(new ItemStack(builderBlock, 1), new Object[]{"btb", "ycy", "gCg", 'b',
//			new ItemStack(Item.dyePowder, 1, 0), 't', markerBlock, 'y', new ItemStack(Item.dyePowder, 1, 11),
//			'c', Block.workbench, 'g', BuildCraftCore.diamondGearItem, 'C', Block.chest});

//		CoreProxy.proxy.addCraftingRecipe(new ItemStack(architectBlock, 1), new Object[]{"btb", "ycy", "gCg", 'b',
//			new ItemStack(Item.dyePowder, 1, 0), 't', markerBlock, 'y', new ItemStack(Item.dyePowder, 1, 11),
//			'c', Block.workbench, 'g', BuildCraftCore.diamondGearItem, 'C',
//			new ItemStack(templateItem, 1)});

//		CoreProxy.proxy.addCraftingRecipe(new ItemStack(libraryBlock, 1), new Object[]{"bbb", "bBb", "bbb", 'b',
//			new ItemStack(blueprintItem), 'B', Block.bookShelf});
	}

	@EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
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

	@EventHandler
	public void ServerStop(FMLServerStoppingEvent event) {
		TilePathMarker.clearAvailableMarkersList();
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void loadTextures(TextureStitchEvent.Pre evt) {
		if (evt.map.textureType == 0) {
			for (FillerPattern pattern : FillerPattern.patterns) {
				pattern.registerIcon(evt.map);
			}
		}
	}
}

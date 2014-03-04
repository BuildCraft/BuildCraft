/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import java.io.File;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import buildcraft.api.blueprints.BlueprintManager;
import buildcraft.api.bptblocks.BptBlockBed;
import buildcraft.api.bptblocks.BptBlockCustomStack;
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
import buildcraft.builders.BuilderProxy;
import buildcraft.builders.EventHandlerBuilders;
import buildcraft.builders.GuiHandler;
import buildcraft.builders.ItemBlueprintStandard;
import buildcraft.builders.ItemBlueprintTemplate;
import buildcraft.builders.TileArchitect;
import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.builders.TileBuilder;
import buildcraft.builders.TileFiller;
import buildcraft.builders.TileMarker;
import buildcraft.builders.TilePathMarker;
import buildcraft.builders.blueprints.BlueprintDatabase;
import buildcraft.builders.filler.FillerRegistry;
import buildcraft.builders.filler.pattern.FillerPattern;
import buildcraft.builders.filler.pattern.PatternBox;
import buildcraft.builders.filler.pattern.PatternClear;
import buildcraft.builders.filler.pattern.PatternCylinder;
import buildcraft.builders.filler.pattern.PatternFill;
import buildcraft.builders.filler.pattern.PatternFlatten;
import buildcraft.builders.filler.pattern.PatternHorizon;
import buildcraft.builders.filler.pattern.PatternPyramid;
import buildcraft.builders.filler.pattern.PatternStairs;
import buildcraft.builders.network.PacketHandlerBuilders;
import buildcraft.builders.triggers.ActionFiller;
import buildcraft.builders.triggers.BuildersActionProvider;
import buildcraft.builders.urbanism.BlockUrbanist;
import buildcraft.builders.urbanism.TileUrbanist;
import buildcraft.builders.urbanism.UrbanistToolsIconProvider;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BCLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(name = "BuildCraft Builders", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Builders", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftBuilders extends BuildCraftMod {

	public static final char BPT_SEP_CHARACTER = '-';
	public static final int LIBRARY_PAGE_SIZE = 12;
	public static final int MAX_BLUEPRINTS_NAME_SIZE = 14;
	public static BlockMarker markerBlock;
	public static BlockPathMarker pathMarkerBlock;
	public static BlockFiller fillerBlock;
	public static BlockBuilder builderBlock;
	public static BlockArchitect architectBlock;
	public static BlockBlueprintLibrary libraryBlock;
	public static BlockUrbanist urbanistBlock;
	public static ItemBlueprintTemplate templateItem;
	public static ItemBlueprintStandard blueprintItem;
	public static boolean fillerDestroy;
	public static int fillerLifespanTough;
	public static int fillerLifespanNormal;
	public static ActionFiller[] fillerActions;
	@Instance("BuildCraft|Builders")
	public static BuildCraftBuilders instance;

	public static BlueprintDatabase serverDB;
	public static BlueprintDatabase clientDB;

	@EventHandler
	public void loadConfiguration(FMLPreInitializationEvent evt) {
		File bptMainDir = new File(new File(evt.getModConfigurationDirectory(), "buildcraft"), "blueprints");

		File serverDir = new File (bptMainDir, "server");
		File clientDir = new File (bptMainDir, "client");

		serverDB = new BlueprintDatabase();
		clientDB = new BlueprintDatabase();

		serverDB.init(serverDir);
		clientDB.init(clientDir);
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		channels = NetworkRegistry.INSTANCE.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-BUILDERS", new PacketHandlerBuilders());

		// Register gui handler
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

		// Register save handler
		MinecraftForge.EVENT_BUS.register(new EventHandlerBuilders());

		BlueprintManager.registerSchematicClass(Blocks.snow, BptBlockIgnore.class);
		BlueprintManager.registerSchematicClass(Blocks.tallgrass, BptBlockIgnore.class);
		BlueprintManager.registerSchematicClass(Blocks.ice, BptBlockIgnore.class);
		BlueprintManager.registerSchematicClass(Blocks.piston_head, BptBlockIgnore.class);

		BlueprintManager.registerSchematicClass(Blocks.dirt, BptBlockDirt.class);
		BlueprintManager.registerSchematicClass(Blocks.grass, BptBlockDirt.class);
		BlueprintManager.registerSchematicClass(Blocks.farmland, BptBlockDirt.class);

		BlueprintManager.registerSchematicClass(Blocks.torch, BptBlockWallSide.class);
		BlueprintManager.registerSchematicClass(Blocks.redstone_torch, BptBlockWallSide.class);
		BlueprintManager.registerSchematicClass(Blocks.unlit_redstone_torch, BptBlockWallSide.class);

		BlueprintManager.registerSchematicClass(Blocks.ladder, BptBlockRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		BlueprintManager.registerSchematicClass(Blocks.fence_gate, BptBlockRotateMeta.class, new int[]{0, 1, 2, 3}, true);

		BlueprintManager.registerSchematicClass(Blocks.furnace, BptBlockRotateInventory.class, new int[]{2, 5, 3, 4}, true);
		BlueprintManager.registerSchematicClass(Blocks.lit_furnace, BptBlockRotateInventory.class, new int[]{2, 5, 3, 4}, true);
		BlueprintManager.registerSchematicClass(Blocks.chest, BptBlockRotateInventory.class, new int[]{2, 5, 3, 4}, true);
		BlueprintManager.registerSchematicClass(Blocks.dispenser, BptBlockRotateInventory.class, new int[]{2, 5, 3, 4}, true);

		BlueprintManager.registerSchematicClass(Blocks.brewing_stand, BptBlockInventory.class);

		BlueprintManager.registerSchematicClass(Blocks.vine, BptBlockRotateMeta.class, new int[]{1, 4, 8, 2}, false);
		BlueprintManager.registerSchematicClass(Blocks.trapdoor, BptBlockRotateMeta.class, new int[]{0, 1, 2, 3}, false);

		BlueprintManager.registerSchematicClass(Blocks.wooden_button, BptBlockLever.class);
		BlueprintManager.registerSchematicClass(Blocks.stone_button, BptBlockLever.class);
		BlueprintManager.registerSchematicClass(Blocks.lever, BptBlockLever.class);

		BlueprintManager.registerSchematicClass(Blocks.stone, BptBlockCustomStack.class, new ItemStack(Blocks.stone));
		BlueprintManager.registerSchematicClass(Blocks.redstone_wire, BptBlockCustomStack.class, new ItemStack(Items.redstone));
		BlueprintManager.registerSchematicClass(Blocks.cake, BptBlockCustomStack.class, new ItemStack(Items.cake));
		//new BptBlockCustomStack(Blocks.crops.blockID, new ItemStack(Items.seeds));
		BlueprintManager.registerSchematicClass(Blocks.pumpkin_stem, BptBlockCustomStack.class, new ItemStack(Items.pumpkin_seeds));
		BlueprintManager.registerSchematicClass(Blocks.melon_stem, BptBlockCustomStack.class, new ItemStack(Items.melon_seeds));
		BlueprintManager.registerSchematicClass(Blocks.glowstone, BptBlockCustomStack.class, new ItemStack(Blocks.glowstone));

		BlueprintManager.registerSchematicClass(Blocks.powered_repeater, BptBlockRedstoneRepeater.class);
		BlueprintManager.registerSchematicClass(Blocks.unpowered_repeater, BptBlockRedstoneRepeater.class);

		BlueprintManager.registerSchematicClass(Blocks.water, BptBlockFluid.class, new ItemStack(Items.water_bucket));
		BlueprintManager.registerSchematicClass(Blocks.flowing_water, BptBlockFluid.class, new ItemStack(Items.water_bucket));
		BlueprintManager.registerSchematicClass(Blocks.lava, BptBlockFluid.class, new ItemStack(Items.lava_bucket));
		BlueprintManager.registerSchematicClass(Blocks.flowing_lava, BptBlockFluid.class, new ItemStack(Items.lava_bucket));

		BlueprintManager.registerSchematicClass(Blocks.rail, BptBlockIgnoreMeta.class);
		BlueprintManager.registerSchematicClass(Blocks.detector_rail, BptBlockIgnoreMeta.class);
		BlueprintManager.registerSchematicClass(Blocks.glass_pane, BptBlockIgnoreMeta.class);

		BlueprintManager.registerSchematicClass(Blocks.piston, BptBlockPiston.class);
		BlueprintManager.registerSchematicClass(Blocks.piston_extension, BptBlockPiston.class);
		BlueprintManager.registerSchematicClass(Blocks.sticky_piston, BptBlockPiston.class);

		BlueprintManager.registerSchematicClass(Blocks.lit_pumpkin, BptBlockPumpkin.class);

		BlueprintManager.registerSchematicClass(Blocks.stone_stairs, BptBlockStairs.class);
		BlueprintManager.registerSchematicClass(Blocks.oak_stairs, BptBlockStairs.class);
		BlueprintManager.registerSchematicClass(Blocks.nether_brick_stairs, BptBlockStairs.class);
		BlueprintManager.registerSchematicClass(Blocks.brick_stairs, BptBlockStairs.class);
		BlueprintManager.registerSchematicClass(Blocks.stone_brick_stairs, BptBlockStairs.class);

		BlueprintManager.registerSchematicClass(Blocks.wooden_button, BptBlockDoor.class, new ItemStack(Items.wooden_door));
		BlueprintManager.registerSchematicClass(Blocks.iron_door, BptBlockDoor.class, new ItemStack(Items.iron_door));

		BlueprintManager.registerSchematicClass(Blocks.bed, BptBlockBed.class);

		BlueprintManager.registerSchematicClass(Blocks.wall_sign, BptBlockSign.class, true);
		BlueprintManager.registerSchematicClass(Blocks.standing_sign, BptBlockSign.class, false);

		// BUILDCRAFT BLOCKS

		BlueprintManager.registerSchematicClass(architectBlock, BptBlockRotateInventory.class, new int[]{2, 5, 3, 4}, true);
		BlueprintManager.registerSchematicClass(builderBlock, BptBlockRotateInventory.class, new int[]{2, 5, 3, 4}, true);

		BlueprintManager.registerSchematicClass(libraryBlock, BptBlockInventory.class);

		BlueprintManager.registerSchematicClass(markerBlock, BptBlockWallSide.class);
		BlueprintManager.registerSchematicClass(pathMarkerBlock, BptBlockWallSide.class);

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		BuilderProxy.proxy.registerBlockRenderers();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		Property fillerDestroyProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "filler.destroy", DefaultProps.FILLER_DESTROY);
		fillerDestroyProp.comment = "If true, Filler will destroy blocks instead of breaking them.";
		fillerDestroy = fillerDestroyProp.getBoolean(DefaultProps.FILLER_DESTROY);

		Property fillerLifespanToughProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "filler.lifespan.tough", DefaultProps.FILLER_LIFESPAN_TOUGH);
		fillerLifespanToughProp.comment = "Lifespan in ticks of items dropped by the filler from 'tough' blocks (those that can't be broken by hand)";
		fillerLifespanTough = fillerLifespanToughProp.getInt(DefaultProps.FILLER_LIFESPAN_TOUGH);

		Property fillerLifespanNormalProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "filler.lifespan.other", DefaultProps.FILLER_LIFESPAN_NORMAL);
		fillerLifespanNormalProp.comment = "Lifespan in ticks of items dropped by the filler from non-tough blocks (those that can be broken by hand)";
		fillerLifespanNormal = fillerLifespanNormalProp.getInt(DefaultProps.FILLER_LIFESPAN_NORMAL);


		templateItem = new ItemBlueprintTemplate();
		templateItem.setUnlocalizedName("templateItem");
		LanguageRegistry.addName(templateItem, "Template");
		CoreProxy.proxy.registerItem(templateItem);

		blueprintItem = new ItemBlueprintStandard();
		blueprintItem.setUnlocalizedName("blueprintItem");
		LanguageRegistry.addName(blueprintItem, "Blueprint");
		CoreProxy.proxy.registerItem(blueprintItem);

		markerBlock = new BlockMarker();
		CoreProxy.proxy.registerBlock(markerBlock.setBlockName("markerBlock"));

		pathMarkerBlock = new BlockPathMarker();
		CoreProxy.proxy.registerBlock(pathMarkerBlock.setBlockName("pathMarkerBlock"));

		fillerBlock = new BlockFiller();
		CoreProxy.proxy.registerBlock(fillerBlock.setBlockName("fillerBlock"));

		builderBlock = new BlockBuilder();
		CoreProxy.proxy.registerBlock(builderBlock.setBlockName("builderBlock"));

		architectBlock = new BlockArchitect();
		CoreProxy.proxy.registerBlock(architectBlock.setBlockName("architectBlock"));

		libraryBlock = new BlockBlueprintLibrary();
		CoreProxy.proxy.registerBlock(libraryBlock.setBlockName("libraryBlock"));

		urbanistBlock = new BlockUrbanist ();
		CoreProxy.proxy.registerBlock(urbanistBlock.setBlockName("urbanistBlock"));
		CoreProxy.proxy.registerTileEntity(TileUrbanist.class, "net.minecraft.src.builders.TileUrbanist");

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
			FillerManager.registry.addPattern(new PatternCylinder());
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
			new ItemStack(Items.dye, 1, 4), 'p', Items.paper});

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(markerBlock, 1), new Object[]{"l ", "r ", 'l',
			new ItemStack(Items.dye, 1, 4), 'r', Blocks.redstone_torch});

//		CoreProxy.proxy.addCraftingRecipe(new ItemStack(pathMarkerBlock, 1), new Object[]{"l ", "r ", 'l',
//			new ItemStack(Item.dyePowder, 1, 2), 'r', Block.torchRedstoneActive});

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(fillerBlock, 1), new Object[]{"btb", "ycy", "gCg", 'b',
			new ItemStack(Items.dye, 1, 0), 't', markerBlock, 'y', new ItemStack(Items.dye, 1, 11),
			'c', Blocks.crafting_table, 'g', BuildCraftCore.goldGearItem, 'C', Blocks.chest});

		//		CoreProxy.proxy.addCraftingRecipe(new ItemStack(builderBlock, 1), new Object[]{"btb", "ycy", "gCg", 'b',
//			new ItemStack(Item.dyePowder, 1, 0), 't', markerBlock, 'y', new ItemStack(Item.dyePowder, 1, 11),
//			'c', Block.workbench, 'g', BuildCraftCore.diamondGearItem, 'C', Block.chest});

//		CoreProxy.proxy.addCraftingRecipe(new ItemStack(architectBlock, 1), new Object[]{"btb", "ycy", "gCg", 'b',
//			new ItemStack(Item.dyePowder, 1, 0), 't', markerBlock, 'y', new ItemStack(Item.dyePowder, 1, 11),
//			'c', Block.workbench, 'g', BuildCraftCore.diamondGearItem, 'C',
//			new ItemStack(templateItem, 1)});

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(libraryBlock, 1), new Object[]{"bbb", "bBb", "bbb", 'b',
			new ItemStack(blueprintItem), 'B', Blocks.bookshelf});
	}

	@EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}

	@EventHandler
	public void ServerStop(FMLServerStoppingEvent event) {
		TilePathMarker.clearAvailableMarkersList();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void loadTextures(TextureStitchEvent.Pre evt) {
		if (evt.map.getTextureType() == 0) {
			for (FillerPattern pattern : FillerPattern.patterns.values()) {
				pattern.registerIcon(evt.map);
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() == 1) {
			UrbanistToolsIconProvider.INSTANCE.registerIcons(event.map);
		}
	}
}

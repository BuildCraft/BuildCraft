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
import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.schematics.SchematicBed;
import buildcraft.api.schematics.SchematicCustomStack;
import buildcraft.api.schematics.SchematicDirt;
import buildcraft.api.schematics.SchematicDoor;
import buildcraft.api.schematics.SchematicFluid;
import buildcraft.api.schematics.SchematicIgnore;
import buildcraft.api.schematics.SchematicIgnoreMeta;
import buildcraft.api.schematics.SchematicInventory;
import buildcraft.api.schematics.SchematicLever;
import buildcraft.api.schematics.SchematicPiston;
import buildcraft.api.schematics.SchematicPumpkin;
import buildcraft.api.schematics.SchematicRedstoneRepeater;
import buildcraft.api.schematics.SchematicRotateInventory;
import buildcraft.api.schematics.SchematicRotateMeta;
import buildcraft.api.schematics.SchematicSign;
import buildcraft.api.schematics.SchematicStairs;
import buildcraft.api.schematics.SchematicWallSide;
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

		BlueprintManager.registerSchematicClass(Blocks.snow, SchematicIgnore.class);
		BlueprintManager.registerSchematicClass(Blocks.tallgrass, SchematicIgnore.class);
		BlueprintManager.registerSchematicClass(Blocks.ice, SchematicIgnore.class);
		BlueprintManager.registerSchematicClass(Blocks.piston_head, SchematicIgnore.class);

		BlueprintManager.registerSchematicClass(Blocks.dirt, SchematicDirt.class);
		BlueprintManager.registerSchematicClass(Blocks.grass, SchematicDirt.class);
		BlueprintManager.registerSchematicClass(Blocks.farmland, SchematicDirt.class);

		BlueprintManager.registerSchematicClass(Blocks.torch, SchematicWallSide.class);
		BlueprintManager.registerSchematicClass(Blocks.redstone_torch, SchematicWallSide.class);
		BlueprintManager.registerSchematicClass(Blocks.unlit_redstone_torch, SchematicWallSide.class);

		BlueprintManager.registerSchematicClass(Blocks.ladder, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		BlueprintManager.registerSchematicClass(Blocks.fence_gate, SchematicRotateMeta.class, new int[]{0, 1, 2, 3}, true);
		BlueprintManager.registerSchematicClass(Blocks.log, SchematicRotateMeta.class, new int[]{8, 4, 8, 4}, true);
		BlueprintManager.registerSchematicClass(Blocks.log2, SchematicRotateMeta.class, new int[]{8, 4, 8, 4}, true);
		BlueprintManager.registerSchematicClass(Blocks.hay_block, SchematicRotateMeta.class, new int[]{8, 4, 8, 4}, true);
		BlueprintManager.registerSchematicClass(Blocks.quartz_block, SchematicRotateMeta.class, new int[]{4, 3, 4, 3}, true);

		BlueprintManager.registerSchematicClass(Blocks.furnace, SchematicRotateInventory.class, new int[]{2, 5, 3, 4}, true);
		BlueprintManager.registerSchematicClass(Blocks.lit_furnace, SchematicRotateInventory.class, new int[]{2, 5, 3, 4}, true);
		BlueprintManager.registerSchematicClass(Blocks.chest, SchematicRotateInventory.class, new int[]{2, 5, 3, 4}, true);
		BlueprintManager.registerSchematicClass(Blocks.dispenser, SchematicRotateInventory.class, new int[]{2, 5, 3, 4}, true);

		BlueprintManager.registerSchematicClass(Blocks.brewing_stand, SchematicInventory.class);

		BlueprintManager.registerSchematicClass(Blocks.vine, SchematicRotateMeta.class, new int[]{1, 4, 8, 2}, false);
		BlueprintManager.registerSchematicClass(Blocks.trapdoor, SchematicRotateMeta.class, new int[]{0, 1, 2, 3}, false);

		BlueprintManager.registerSchematicClass(Blocks.wooden_button, SchematicLever.class);
		BlueprintManager.registerSchematicClass(Blocks.stone_button, SchematicLever.class);
		BlueprintManager.registerSchematicClass(Blocks.lever, SchematicLever.class);

		BlueprintManager.registerSchematicClass(Blocks.stone, SchematicCustomStack.class, new ItemStack(Blocks.stone));
		BlueprintManager.registerSchematicClass(Blocks.redstone_wire, SchematicCustomStack.class, new ItemStack(Items.redstone));
		BlueprintManager.registerSchematicClass(Blocks.cake, SchematicCustomStack.class, new ItemStack(Items.cake));
		//new BptBlockCustomStack(Blocks.crops.blockID, new ItemStack(Items.seeds));
		BlueprintManager.registerSchematicClass(Blocks.pumpkin_stem, SchematicCustomStack.class, new ItemStack(Items.pumpkin_seeds));
		BlueprintManager.registerSchematicClass(Blocks.melon_stem, SchematicCustomStack.class, new ItemStack(Items.melon_seeds));
		BlueprintManager.registerSchematicClass(Blocks.glowstone, SchematicCustomStack.class, new ItemStack(Blocks.glowstone));

		BlueprintManager.registerSchematicClass(Blocks.powered_repeater, SchematicRedstoneRepeater.class);
		BlueprintManager.registerSchematicClass(Blocks.unpowered_repeater, SchematicRedstoneRepeater.class);

		BlueprintManager.registerSchematicClass(Blocks.water, SchematicFluid.class, new ItemStack(Items.water_bucket));
		BlueprintManager.registerSchematicClass(Blocks.flowing_water, SchematicFluid.class, new ItemStack(Items.water_bucket));
		BlueprintManager.registerSchematicClass(Blocks.lava, SchematicFluid.class, new ItemStack(Items.lava_bucket));
		BlueprintManager.registerSchematicClass(Blocks.flowing_lava, SchematicFluid.class, new ItemStack(Items.lava_bucket));

		BlueprintManager.registerSchematicClass(Blocks.rail, SchematicIgnoreMeta.class);
		BlueprintManager.registerSchematicClass(Blocks.detector_rail, SchematicIgnoreMeta.class);
		BlueprintManager.registerSchematicClass(Blocks.glass_pane, SchematicIgnoreMeta.class);

		BlueprintManager.registerSchematicClass(Blocks.piston, SchematicPiston.class);
		BlueprintManager.registerSchematicClass(Blocks.piston_extension, SchematicPiston.class);
		BlueprintManager.registerSchematicClass(Blocks.sticky_piston, SchematicPiston.class);

		BlueprintManager.registerSchematicClass(Blocks.lit_pumpkin, SchematicPumpkin.class);

		BlueprintManager.registerSchematicClass(Blocks.stone_stairs, SchematicStairs.class);
		BlueprintManager.registerSchematicClass(Blocks.oak_stairs, SchematicStairs.class);
		BlueprintManager.registerSchematicClass(Blocks.nether_brick_stairs, SchematicStairs.class);
		BlueprintManager.registerSchematicClass(Blocks.brick_stairs, SchematicStairs.class);
		BlueprintManager.registerSchematicClass(Blocks.stone_brick_stairs, SchematicStairs.class);

		BlueprintManager.registerSchematicClass(Blocks.wooden_button, SchematicDoor.class, new ItemStack(Items.wooden_door));
		BlueprintManager.registerSchematicClass(Blocks.iron_door, SchematicDoor.class, new ItemStack(Items.iron_door));

		BlueprintManager.registerSchematicClass(Blocks.bed, SchematicBed.class);

		BlueprintManager.registerSchematicClass(Blocks.wall_sign, SchematicSign.class, true);
		BlueprintManager.registerSchematicClass(Blocks.standing_sign, SchematicSign.class, false);

		// BUILDCRAFT BLOCKS

		BlueprintManager.registerSchematicClass(architectBlock, SchematicRotateInventory.class, new int[]{2, 5, 3, 4}, true);
		BlueprintManager.registerSchematicClass(builderBlock, SchematicRotateInventory.class, new int[]{2, 5, 3, 4}, true);

		BlueprintManager.registerSchematicClass(libraryBlock, SchematicInventory.class);

		BlueprintManager.registerSchematicClass(markerBlock, SchematicWallSide.class);
		BlueprintManager.registerSchematicClass(pathMarkerBlock, SchematicWallSide.class);

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

		if (!BuildCraftCore.NEXTGEN_PREALPHA) {
			urbanistBlock = new BlockUrbanist ();
			CoreProxy.proxy.registerBlock(urbanistBlock.setBlockName("urbanistBlock"));
			CoreProxy.proxy.registerTileEntity(TileUrbanist.class, "net.minecraft.src.builders.TileUrbanist");
		}

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

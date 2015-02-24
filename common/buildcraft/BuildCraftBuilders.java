/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.item.EntityMinecartFurnace;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import buildcraft.api.blueprints.BlueprintDeployer;
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.ISchematicRegistry;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.blueprints.SchematicFactory;
import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.JavaTools;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.StatementManager;
import buildcraft.builders.BlockArchitect;
import buildcraft.builders.BlockBlueprintLibrary;
import buildcraft.builders.BlockBuildTool;
import buildcraft.builders.BlockBuilder;
import buildcraft.builders.BlockConstructionMarker;
import buildcraft.builders.BlockFiller;
import buildcraft.builders.BlockMarker;
import buildcraft.builders.BlockPathMarker;
import buildcraft.builders.BuilderProxy;
import buildcraft.builders.EventHandlerBuilders;
import buildcraft.builders.GuiHandler;
import buildcraft.builders.HeuristicBlockDetection;
import buildcraft.builders.ItemBlueprintStandard;
import buildcraft.builders.ItemBlueprintTemplate;
import buildcraft.builders.ItemConstructionMarker;
import buildcraft.builders.TileArchitect;
import buildcraft.builders.TileBlueprintLibrary;
import buildcraft.builders.TileBuilder;
import buildcraft.builders.TileConstructionMarker;
import buildcraft.builders.TileFiller;
import buildcraft.builders.TileMarker;
import buildcraft.builders.TilePathMarker;
import buildcraft.builders.blueprints.BlueprintDatabase;
import buildcraft.builders.schematics.SchematicAir;
import buildcraft.builders.schematics.SchematicBed;
import buildcraft.builders.schematics.SchematicBlockCreative;
import buildcraft.builders.schematics.SchematicCactus;
import buildcraft.builders.schematics.SchematicCustomStack;
import buildcraft.builders.schematics.SchematicDirt;
import buildcraft.builders.schematics.SchematicDoor;
import buildcraft.builders.schematics.SchematicEnderChest;
import buildcraft.builders.schematics.SchematicFactoryBlock;
import buildcraft.builders.schematics.SchematicFactoryEntity;
import buildcraft.builders.schematics.SchematicFactoryMask;
import buildcraft.builders.schematics.SchematicFarmland;
import buildcraft.builders.schematics.SchematicFire;
import buildcraft.builders.schematics.SchematicGlassPane;
import buildcraft.builders.schematics.SchematicGravel;
import buildcraft.builders.schematics.SchematicHanging;
import buildcraft.builders.schematics.SchematicIgnore;
import buildcraft.builders.schematics.SchematicLever;
import buildcraft.builders.schematics.SchematicMinecart;
import buildcraft.builders.schematics.SchematicPiston;
import buildcraft.builders.schematics.SchematicPortal;
import buildcraft.builders.schematics.SchematicPumpkin;
import buildcraft.builders.schematics.SchematicRail;
import buildcraft.builders.schematics.SchematicRedstoneDiode;
import buildcraft.builders.schematics.SchematicRedstoneLamp;
import buildcraft.builders.schematics.SchematicRedstoneWire;
import buildcraft.builders.schematics.SchematicRotateMeta;
import buildcraft.builders.schematics.SchematicSeeds;
import buildcraft.builders.schematics.SchematicSign;
import buildcraft.builders.schematics.SchematicSkull;
import buildcraft.builders.schematics.SchematicStairs;
import buildcraft.builders.schematics.SchematicStandalone;
import buildcraft.builders.schematics.SchematicStone;
import buildcraft.builders.schematics.SchematicTileCreative;
import buildcraft.builders.schematics.SchematicTripWireHook;
import buildcraft.builders.schematics.SchematicWallSide;
import buildcraft.builders.statements.ActionFiller;
import buildcraft.builders.statements.BuildersActionProvider;
import buildcraft.builders.urbanism.BlockUrbanist;
import buildcraft.builders.urbanism.TileUrbanist;
import buildcraft.builders.urbanism.UrbanistToolsIconProvider;
import buildcraft.compat.CompatHooks;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.blueprints.RealBlueprintDeployer;
import buildcraft.core.blueprints.SchematicRegistry;
import buildcraft.core.builders.patterns.FillerPattern;
import buildcraft.core.builders.patterns.FillerRegistry;
import buildcraft.core.builders.patterns.PatternBox;
import buildcraft.core.builders.patterns.PatternClear;
import buildcraft.core.builders.patterns.PatternCylinder;
import buildcraft.core.builders.patterns.PatternFill;
import buildcraft.core.builders.patterns.PatternFlatten;
import buildcraft.core.builders.patterns.PatternFrame;
import buildcraft.core.builders.patterns.PatternHorizon;
import buildcraft.core.builders.patterns.PatternParameterYDir;
import buildcraft.core.builders.patterns.PatternPyramid;
import buildcraft.core.builders.patterns.PatternStairs;
import buildcraft.core.proxy.CoreProxy;

@Mod(name = "BuildCraft Builders", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Builders", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftBuilders extends BuildCraftMod {

	@Mod.Instance("BuildCraft|Builders")
	public static BuildCraftBuilders instance;

	public static final char BPT_SEP_CHARACTER = '-';
	public static final int LIBRARY_PAGE_SIZE = 12;
	public static final int MAX_BLUEPRINTS_NAME_SIZE = 32;
	public static BlockBuildTool buildToolBlock;
	public static BlockMarker markerBlock;
	public static BlockPathMarker pathMarkerBlock;
	public static BlockConstructionMarker constructionMarkerBlock;
	public static BlockFiller fillerBlock;
	public static BlockBuilder builderBlock;
	public static BlockArchitect architectBlock;
	public static BlockBlueprintLibrary libraryBlock;
	public static BlockUrbanist urbanistBlock;
	public static ItemBlueprintTemplate templateItem;
	public static ItemBlueprintStandard blueprintItem;

	public static ActionFiller[] fillerActions;

	public static BlueprintDatabase serverDB;
	public static BlueprintDatabase clientDB;

	public static boolean debugPrintSchematicList = false;
	
	@Mod.EventHandler
	public void loadConfiguration(FMLPreInitializationEvent evt) {
		String blueprintServerDir = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL,
				"blueprints.serverDir",
				"\"$MINECRAFT" + File.separator + "config" + File.separator + "buildcraft" + File.separator
						+ "blueprints" + File.separator + "server\"").getString();

		String blueprintLibraryOutput = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL,
				"blueprints.libraryOutput", "\"$MINECRAFT" + File.separator + "blueprints\"").getString();

		String [] blueprintLibraryInput = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL,
				"blueprints.libraryInput", new String []
				{
						// expected location
						"\"$MINECRAFT" + File.separator + "blueprints\"",
						// legacy beta BuildCraft
						"\"$MINECRAFT" + File.separator + "config" + File.separator + "buildcraft" + File.separator
								+ "blueprints" + File.separator + "client\"",
						// inferred user download location
						"\"" + getDownloadsDir() + "\""
				}
				).getStringList().clone();

		blueprintServerDir = JavaTools.stripSurroundingQuotes(replacePathVariables(blueprintServerDir));
		blueprintLibraryOutput = JavaTools.stripSurroundingQuotes(replacePathVariables(blueprintLibraryOutput));

		for (int i = 0; i < blueprintLibraryInput.length; ++i) {
			blueprintLibraryInput[i] = JavaTools.stripSurroundingQuotes(replacePathVariables(blueprintLibraryInput[i]));
		}
		
		Property printSchematicList = BuildCraftCore.mainConfiguration.get("debug", "blueprints.printSchematicList", false);
		debugPrintSchematicList = printSchematicList.getBoolean();

		if (BuildCraftCore.mainConfiguration.hasChanged()) {
			BuildCraftCore.mainConfiguration.save();
		}

		serverDB = new BlueprintDatabase();
		clientDB = new BlueprintDatabase();

		serverDB.init(new String[] {blueprintServerDir}, blueprintServerDir);
		clientDB.init(blueprintLibraryInput, blueprintLibraryOutput);
	}

	private static String getDownloadsDir() {
		final String os = System.getProperty("os.name").toLowerCase();

		if (os.contains("nix") || os.contains("lin") || os.contains("mac")) {
			// Linux, Mac or other UNIX
			// According XDG specification every user-specified folder can be localized
			// or even moved to any destination, so we obtain real path with xdg-user-dir
			try {
				Process process = Runtime.getRuntime().exec(new String[] {"xdg-user-dir", "DOWNLOAD"});
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
				process.waitFor();
				String line = reader.readLine().trim();
				reader.close();

				if (line.length() > 0) {
					return line;
				}
			} catch (Exception ignored) {
				// Very bad, we have a error while obtaining xdg dir :(
				// Just ignore, uses default dir
			}
		}
		// Windows or unknown system
		return "$HOME" + File.separator + "Downloads";
	}

	private String replacePathVariables(String path) {
		String result = path.replace("$DOWNLOADS", getDownloadsDir());
		result = result.replace("$HOME", System.getProperty("user.home"));

		if (Launch.minecraftHome == null) {
			result = result.replace("$MINECRAFT", new File(".").getAbsolutePath());
		} else {
			result = result.replace("$MINECRAFT", Launch.minecraftHome.getAbsolutePath());
		}

		if ("/".equals(File.separator)) {
			result = result.replaceAll("\\\\", "/");
		} else {
			result = result.replaceAll("/", "\\\\");
		}
		
		return result;
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		HeuristicBlockDetection.start();
		
		if (debugPrintSchematicList) {
			try {
				PrintWriter writer = new PrintWriter("SchematicDebug.txt", "UTF-8");
				writer.println("*** REGISTERED SCHEMATICS ***");
				SchematicRegistry reg = (SchematicRegistry) BuilderAPI.schematicRegistry;
				for (String s : reg.schematicBlocks.keySet()) {
					writer.println(s + " -> " + reg.schematicBlocks.get(s).clazz.getCanonicalName());
				}
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		// Register gui handler
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

		// Register save handler
		MinecraftForge.EVENT_BUS.register(new EventHandlerBuilders());

		// Standard blocks
		ISchematicRegistry schemes = BuilderAPI.schematicRegistry;
		schemes.registerSchematicBlock(Blocks.air, SchematicAir.class);

		schemes.registerSchematicBlock(Blocks.snow, SchematicIgnore.class);
		schemes.registerSchematicBlock(Blocks.tallgrass, SchematicIgnore.class);
		schemes.registerSchematicBlock(Blocks.double_plant, SchematicIgnore.class);
		schemes.registerSchematicBlock(Blocks.ice, SchematicIgnore.class);
		schemes.registerSchematicBlock(Blocks.piston_head, SchematicIgnore.class);

		schemes.registerSchematicBlock(Blocks.dirt, SchematicDirt.class);
		schemes.registerSchematicBlock(Blocks.grass, SchematicDirt.class);

		schemes.registerSchematicBlock(Blocks.cactus, SchematicCactus.class);

		schemes.registerSchematicBlock(Blocks.farmland, SchematicFarmland.class);
		schemes.registerSchematicBlock(Blocks.wheat, SchematicSeeds.class, Items.wheat_seeds);
		schemes.registerSchematicBlock(Blocks.pumpkin_stem, SchematicSeeds.class, Items.pumpkin_seeds);
		schemes.registerSchematicBlock(Blocks.melon_stem, SchematicSeeds.class, Items.melon_seeds);
		schemes.registerSchematicBlock(Blocks.nether_wart, SchematicSeeds.class, Items.nether_wart);

		schemes.registerSchematicBlock(Blocks.torch, SchematicWallSide.class);
		schemes.registerSchematicBlock(Blocks.redstone_torch, SchematicWallSide.class);
		schemes.registerSchematicBlock(Blocks.unlit_redstone_torch, SchematicWallSide.class);

		schemes.registerSchematicBlock(Blocks.tripwire_hook, SchematicTripWireHook.class);

		schemes.registerSchematicBlock(Blocks.skull, SchematicSkull.class);

		schemes.registerSchematicBlock(Blocks.ladder, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		schemes.registerSchematicBlock(Blocks.fence_gate, SchematicRotateMeta.class, new int[]{0, 1, 2, 3}, true);
		schemes.registerSchematicBlock(Blocks.log, SchematicRotateMeta.class, new int[]{8, 4, 8, 4}, true);
		schemes.registerSchematicBlock(Blocks.log2, SchematicRotateMeta.class, new int[]{8, 4, 8, 4}, true);
		schemes.registerSchematicBlock(Blocks.hay_block, SchematicRotateMeta.class, new int[]{8, 4, 8, 4}, true);
		schemes.registerSchematicBlock(Blocks.quartz_block, SchematicRotateMeta.class, new int[]{4, 3, 4, 3}, true);
		schemes.registerSchematicBlock(Blocks.hopper, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		schemes.registerSchematicBlock(Blocks.anvil, SchematicRotateMeta.class, new int[]{0, 1, 2, 3}, true);

		schemes.registerSchematicBlock(Blocks.furnace, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		schemes.registerSchematicBlock(Blocks.lit_furnace, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		schemes.registerSchematicBlock(Blocks.chest, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		schemes.registerSchematicBlock(Blocks.trapped_chest, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		schemes.registerSchematicBlock(Blocks.dispenser, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		schemes.registerSchematicBlock(Blocks.dropper, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);

		schemes.registerSchematicBlock(Blocks.ender_chest, SchematicEnderChest.class);

		schemes.registerSchematicBlock(Blocks.vine, SchematicRotateMeta.class, new int[]{1, 4, 8, 2}, false);
		schemes.registerSchematicBlock(Blocks.trapdoor, SchematicRotateMeta.class, new int[]{0, 1, 2, 3}, false);

		schemes.registerSchematicBlock(Blocks.wooden_button, SchematicLever.class);
		schemes.registerSchematicBlock(Blocks.stone_button, SchematicLever.class);
		schemes.registerSchematicBlock(Blocks.lever, SchematicLever.class);

		schemes.registerSchematicBlock(Blocks.stone, SchematicStone.class);
		schemes.registerSchematicBlock(Blocks.gold_ore, SchematicStone.class);
		schemes.registerSchematicBlock(Blocks.iron_ore, SchematicStone.class);
		schemes.registerSchematicBlock(Blocks.coal_ore, SchematicStone.class);
		schemes.registerSchematicBlock(Blocks.lapis_ore, SchematicStone.class);
		schemes.registerSchematicBlock(Blocks.diamond_ore, SchematicStone.class);
		schemes.registerSchematicBlock(Blocks.redstone_ore, SchematicStone.class);
		schemes.registerSchematicBlock(Blocks.lit_redstone_ore, SchematicStone.class);
		schemes.registerSchematicBlock(Blocks.emerald_ore, SchematicStone.class);

		schemes.registerSchematicBlock(Blocks.gravel, SchematicGravel.class);

		schemes.registerSchematicBlock(Blocks.redstone_wire, SchematicRedstoneWire.class, new ItemStack(Items.redstone));
		schemes.registerSchematicBlock(Blocks.cake, SchematicCustomStack.class, new ItemStack(Items.cake));
		schemes.registerSchematicBlock(Blocks.glowstone, SchematicCustomStack.class, new ItemStack(Blocks.glowstone));

		schemes.registerSchematicBlock(Blocks.powered_repeater, SchematicRedstoneDiode.class, Items.repeater);
		schemes.registerSchematicBlock(Blocks.unpowered_repeater, SchematicRedstoneDiode.class, Items.repeater);
		schemes.registerSchematicBlock(Blocks.powered_comparator, SchematicRedstoneDiode.class, Items.comparator);
		schemes.registerSchematicBlock(Blocks.unpowered_comparator, SchematicRedstoneDiode.class, Items.comparator);

		schemes.registerSchematicBlock(Blocks.redstone_lamp, SchematicRedstoneLamp.class);
		schemes.registerSchematicBlock(Blocks.lit_redstone_lamp, SchematicRedstoneLamp.class);

		schemes.registerSchematicBlock(Blocks.glass_pane, SchematicGlassPane.class);
		schemes.registerSchematicBlock(Blocks.stained_glass_pane, SchematicGlassPane.class);

		schemes.registerSchematicBlock(Blocks.piston, SchematicPiston.class);
		schemes.registerSchematicBlock(Blocks.piston_extension, SchematicPiston.class);
		schemes.registerSchematicBlock(Blocks.sticky_piston, SchematicPiston.class);

		schemes.registerSchematicBlock(Blocks.lit_pumpkin, SchematicPumpkin.class);

		schemes.registerSchematicBlock(Blocks.oak_stairs, SchematicStairs.class);
		schemes.registerSchematicBlock(Blocks.stone_stairs, SchematicStairs.class);
		schemes.registerSchematicBlock(Blocks.brick_stairs, SchematicStairs.class);
		schemes.registerSchematicBlock(Blocks.stone_brick_stairs, SchematicStairs.class);
		schemes.registerSchematicBlock(Blocks.nether_brick_stairs, SchematicStairs.class);
		schemes.registerSchematicBlock(Blocks.sandstone_stairs, SchematicStairs.class);
		schemes.registerSchematicBlock(Blocks.spruce_stairs, SchematicStairs.class);
		schemes.registerSchematicBlock(Blocks.birch_stairs, SchematicStairs.class);
		schemes.registerSchematicBlock(Blocks.jungle_stairs, SchematicStairs.class);
		schemes.registerSchematicBlock(Blocks.quartz_stairs, SchematicStairs.class);
		schemes.registerSchematicBlock(Blocks.acacia_stairs, SchematicStairs.class);
		schemes.registerSchematicBlock(Blocks.dark_oak_stairs, SchematicStairs.class);

		schemes.registerSchematicBlock(Blocks.wooden_door, SchematicDoor.class, new ItemStack(Items.wooden_door));
		schemes.registerSchematicBlock(Blocks.iron_door, SchematicDoor.class, new ItemStack(Items.iron_door));

		schemes.registerSchematicBlock(Blocks.bed, SchematicBed.class);

		schemes.registerSchematicBlock(Blocks.wall_sign, SchematicSign.class, true);
		schemes.registerSchematicBlock(Blocks.standing_sign, SchematicSign.class, false);

		schemes.registerSchematicBlock(Blocks.portal, SchematicPortal.class);

		schemes.registerSchematicBlock(Blocks.rail, SchematicRail.class);
		schemes.registerSchematicBlock(Blocks.activator_rail, SchematicRail.class);
		schemes.registerSchematicBlock(Blocks.detector_rail, SchematicRail.class);
		schemes.registerSchematicBlock(Blocks.golden_rail, SchematicRail.class);

		schemes.registerSchematicBlock(Blocks.fire, SchematicFire.class);

		schemes.registerSchematicBlock(Blocks.bedrock, SchematicBlockCreative.class);

		schemes.registerSchematicBlock(Blocks.mob_spawner, SchematicTileCreative.class);

		schemes.registerSchematicBlock(Blocks.glass, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.stone_slab, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.double_stone_slab, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.wooden_slab, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.double_wooden_slab, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.stained_glass, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.fence, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.daylight_detector, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.iron_bars, SchematicStandalone.class);

		// Standard entities

		schemes.registerSchematicEntity(EntityMinecartEmpty.class, SchematicMinecart.class, Items.minecart);
		schemes.registerSchematicEntity(EntityMinecartFurnace.class, SchematicMinecart.class, Items.furnace_minecart);
		schemes.registerSchematicEntity(EntityMinecartTNT.class, SchematicMinecart.class, Items.tnt_minecart);
		schemes.registerSchematicEntity(EntityMinecartChest.class, SchematicMinecart.class, Items.chest_minecart);
		schemes.registerSchematicEntity(EntityMinecartHopper.class, SchematicMinecart.class, Items.hopper_minecart);

		schemes.registerSchematicEntity(EntityPainting.class, SchematicHanging.class, Items.painting);
		schemes.registerSchematicEntity(EntityItemFrame.class, SchematicHanging.class, Items.item_frame);

		// BuildCraft blocks

		schemes.registerSchematicBlock(architectBlock, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		schemes.registerSchematicBlock(builderBlock, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);

		// Landmarks are often caught incorrectly, making building them counter-productive.
		schemes.registerSchematicBlock(markerBlock, SchematicIgnore.class);
		schemes.registerSchematicBlock(pathMarkerBlock, SchematicIgnore.class);
		schemes.registerSchematicBlock(constructionMarkerBlock, SchematicIgnore.class);

		// Factories required to save entities in world

		SchematicFactory.registerSchematicFactory(SchematicBlock.class, new SchematicFactoryBlock());
		SchematicFactory.registerSchematicFactory(SchematicMask.class, new SchematicFactoryMask());
		SchematicFactory.registerSchematicFactory(SchematicEntity.class, new SchematicFactoryEntity());

		BlueprintDeployer.instance = new RealBlueprintDeployer();

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		BuilderProxy.proxy.registerBlockRenderers();
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		templateItem = new ItemBlueprintTemplate();
		templateItem.setUnlocalizedName("templateItem");
		CoreProxy.proxy.registerItem(templateItem);

		blueprintItem = new ItemBlueprintStandard();
		blueprintItem.setUnlocalizedName("blueprintItem");
		CoreProxy.proxy.registerItem(blueprintItem);

		buildToolBlock = new BlockBuildTool ();
		buildToolBlock.setBlockName("buildToolBlock");
		CoreProxy.proxy.registerBlock(buildToolBlock);

		markerBlock = (BlockMarker) CompatHooks.INSTANCE.getBlock(BlockMarker.class);
		CoreProxy.proxy.registerBlock(markerBlock.setBlockName("markerBlock"));

		pathMarkerBlock = (BlockPathMarker) CompatHooks.INSTANCE.getBlock(BlockPathMarker.class);
		CoreProxy.proxy.registerBlock(pathMarkerBlock.setBlockName("pathMarkerBlock"));

		constructionMarkerBlock = (BlockConstructionMarker) CompatHooks.INSTANCE.getBlock(BlockConstructionMarker.class);
		CoreProxy.proxy.registerBlock(constructionMarkerBlock.setBlockName("constructionMarkerBlock"),
				ItemConstructionMarker.class);

		fillerBlock = (BlockFiller) CompatHooks.INSTANCE.getBlock(BlockFiller.class);
		CoreProxy.proxy.registerBlock(fillerBlock.setBlockName("fillerBlock"));

		builderBlock = (BlockBuilder) CompatHooks.INSTANCE.getBlock(BlockBuilder.class);
		CoreProxy.proxy.registerBlock(builderBlock.setBlockName("builderBlock"));

		architectBlock = (BlockArchitect) CompatHooks.INSTANCE.getBlock(BlockArchitect.class);
		CoreProxy.proxy.registerBlock(architectBlock.setBlockName("architectBlock"));

		libraryBlock = (BlockBlueprintLibrary) CompatHooks.INSTANCE.getBlock(BlockBlueprintLibrary.class);
		CoreProxy.proxy.registerBlock(libraryBlock.setBlockName("libraryBlock"));

		if (!BuildCraftCore.NONRELEASED_BLOCKS) {
			urbanistBlock = new BlockUrbanist ();
			CoreProxy.proxy.registerBlock(urbanistBlock.setBlockName("urbanistBlock"));
			CoreProxy.proxy.registerTileEntity(TileUrbanist.class, "net.minecraft.src.builders.TileUrbanist");
		}

		CoreProxy.proxy.registerTileEntity(TileMarker.class, "Marker");
		CoreProxy.proxy.registerTileEntity(TileFiller.class, "Filler");
		CoreProxy.proxy.registerTileEntity(TileBuilder.class, "net.minecraft.src.builders.TileBuilder");
		CoreProxy.proxy.registerTileEntity(TileArchitect.class, "net.minecraft.src.builders.TileTemplate");
		CoreProxy.proxy.registerTileEntity(TilePathMarker.class, "net.minecraft.src.builders.TilePathMarker");
		CoreProxy.proxy.registerTileEntity(TileConstructionMarker.class, "net.minecraft.src.builders.TileConstructionMarker");
		CoreProxy.proxy.registerTileEntity(TileBlueprintLibrary.class, "net.minecraft.src.builders.TileBlueprintLibrary");

		SchematicRegistry.INSTANCE.readConfiguration(BuildCraftCore.mainConfiguration);

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
			FillerManager.registry.addPattern(new PatternFrame());
		} catch (Error error) {
			BCLog.logErrorAPI("Buildcraft", error, IFillerPattern.class);
			throw error;
		}

		StatementManager.registerActionProvider(new BuildersActionProvider());

		StatementManager.registerParameterClass(PatternParameterYDir.class);
	}

	public static void loadRecipes() {
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(templateItem, 1), "ppp", "pip", "ppp", 'i',
			"dyeBlack", 'p', Items.paper);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(blueprintItem, 1), "ppp", "pip", "ppp", 'i',
			new ItemStack(Items.dye, 1, 4), 'p', Items.paper);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(markerBlock, 1), "l ", "r ", 'l',
			new ItemStack(Items.dye, 1, 4), 'r', Blocks.redstone_torch);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(pathMarkerBlock, 1), "l ", "r ", 'l',
			"dyeGreen", 'r', Blocks.redstone_torch);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(constructionMarkerBlock, 1), "l ", "r ", 'l',
				"gearGold", 'r', Blocks.redstone_torch);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(fillerBlock, 1), "btb", "ycy", "gCg", 'b',
			"dyeBlack", 't', markerBlock, 'y', "dyeYellow",
			'c', Blocks.crafting_table, 'g', "gearGold", 'C', Blocks.chest);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(builderBlock, 1), "btb", "ycy", "gCg", 'b',
			"dyeBlack", 't', markerBlock, 'y', "dyeYellow",
			'c', Blocks.crafting_table, 'g', "gearDiamond", 'C', Blocks.chest);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(architectBlock, 1), "btb", "ycy", "gCg", 'b',
			"dyeBlack", 't', markerBlock, 'y', "dyeYellow",
			'c', Blocks.crafting_table, 'g', "gearDiamond", 'C',
			new ItemStack(blueprintItem, 1));

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(libraryBlock, 1), "bbb", "bBb", "bbb", 'b',
			new ItemStack(blueprintItem), 'B', Blocks.bookshelf);
	}

	@Mod.EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}

	@Mod.EventHandler
	public void serverStop(FMLServerStoppingEvent event) {
		TilePathMarker.clearAvailableMarkersList();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void loadTextures(TextureStitchEvent.Pre evt) {
		if (evt.map.getTextureType() == 0) {
			for (FillerPattern pattern : FillerPattern.patterns.values()) {
				pattern.registerIcons(evt.map);
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

	@Mod.EventHandler
	public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
		//FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
		//		TileMarker.class.getCanonicalName());
		//FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
		//		TileFiller.class.getCanonicalName());
		//FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
		//		TileBuilder.class.getCanonicalName());
		//FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
		//		TileArchitect.class.getCanonicalName());
		//FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
		//		TilePathMarker.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
				TileBlueprintLibrary.class.getCanonicalName());
	}

	@Mod.EventHandler
	public void remap(FMLMissingMappingsEvent event) {
		for (FMLMissingMappingsEvent.MissingMapping mapping: event.get()) {
			if (mapping.name.equals("BuildCraft|Builders:null")) {
				if (mapping.type == GameRegistry.Type.ITEM) {
					mapping.remap(Item.getItemFromBlock(buildToolBlock));
				} else {
					mapping.remap(buildToolBlock);
				}
			}
		}
	}
}

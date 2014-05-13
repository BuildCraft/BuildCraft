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

import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.item.EntityMinecartFurnace;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
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

import buildcraft.api.blueprints.BlueprintDeployer;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.blueprints.SchematicFactory;
import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.blueprints.SchematicRegistry;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.JavaTools;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.gates.ActionManager;
import buildcraft.builders.BlockArchitect;
import buildcraft.builders.BlockBlueprintLibrary;
import buildcraft.builders.BlockBuildTool;
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
import buildcraft.builders.filler.pattern.PatternFrame;
import buildcraft.builders.filler.pattern.PatternHorizon;
import buildcraft.builders.filler.pattern.PatternPyramid;
import buildcraft.builders.filler.pattern.PatternStairs;
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
import buildcraft.builders.schematics.SchematicFluid;
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
import buildcraft.builders.triggers.ActionFiller;
import buildcraft.builders.triggers.BuildersActionProvider;
import buildcraft.builders.urbanism.BlockUrbanist;
import buildcraft.builders.urbanism.TileUrbanist;
import buildcraft.builders.urbanism.UrbanistToolsIconProvider;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.blueprints.RealBlueprintDeployer;
import buildcraft.core.proxy.CoreProxy;

@Mod(name = "BuildCraft Builders", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Builders", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftBuilders extends BuildCraftMod {

	public static final char BPT_SEP_CHARACTER = '-';
	public static final int LIBRARY_PAGE_SIZE = 12;
	public static final int MAX_BLUEPRINTS_NAME_SIZE = 32;
	public static BlockBuildTool buildToolBlock;
	public static BlockMarker markerBlock;
	public static BlockPathMarker pathMarkerBlock;
	public static BlockFiller fillerBlock;
	public static BlockBuilder builderBlock;
	public static BlockArchitect architectBlock;
	public static BlockBlueprintLibrary libraryBlock;
	public static BlockUrbanist urbanistBlock;
	public static ItemBlueprintTemplate templateItem;
	public static ItemBlueprintStandard blueprintItem;
	public static ActionFiller[] fillerActions;
	@Mod.Instance("BuildCraft|Builders")
	public static BuildCraftBuilders instance;

	public static BlueprintDatabase serverDB;
	public static BlueprintDatabase clientDB;

	@Mod.EventHandler
	public void loadConfiguration(FMLPreInitializationEvent evt) {
		File bptMainDir = new File(new File(evt.getModConfigurationDirectory(), "buildcraft"), "blueprints");

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
						// infered used download location
						"\"" + getDownloadsDir() + "\""
				}
				).getStringList().clone();

		blueprintServerDir = JavaTools.stripSurroundingQuotes(replacePathVariables(blueprintServerDir));
		blueprintLibraryOutput = JavaTools.stripSurroundingQuotes(replacePathVariables(blueprintLibraryOutput));

		for (int i = 0; i < blueprintLibraryInput.length; ++i) {
			blueprintLibraryInput[i] = JavaTools.stripSurroundingQuotes(replacePathVariables(blueprintLibraryInput[i]));
		}

		if (BuildCraftCore.mainConfiguration.hasChanged()) {
			BuildCraftCore.mainConfiguration.save();
		}

		File serverDir = new File (bptMainDir, "server");
		File clientDir = new File (bptMainDir, "client");

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

		return result;
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {
		SchematicRegistry.declareBlueprintSupport("BuildCraft|Builders");

		// Register gui handler
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

		// Register save handler
		MinecraftForge.EVENT_BUS.register(new EventHandlerBuilders());

		// Standard blocks

		SchematicRegistry.registerSchematicBlock(Blocks.snow, SchematicIgnore.class);
		SchematicRegistry.registerSchematicBlock(Blocks.tallgrass, SchematicIgnore.class);
		SchematicRegistry.registerSchematicBlock(Blocks.double_plant, SchematicIgnore.class);
		SchematicRegistry.registerSchematicBlock(Blocks.ice, SchematicIgnore.class);
		SchematicRegistry.registerSchematicBlock(Blocks.piston_head, SchematicIgnore.class);

		SchematicRegistry.registerSchematicBlock(Blocks.dirt, SchematicDirt.class);
		SchematicRegistry.registerSchematicBlock(Blocks.grass, SchematicDirt.class);

		SchematicRegistry.registerSchematicBlock(Blocks.cactus, SchematicCactus.class);

		SchematicRegistry.registerSchematicBlock(Blocks.farmland, SchematicFarmland.class);
		SchematicRegistry.registerSchematicBlock(Blocks.wheat, SchematicSeeds.class, Items.wheat_seeds);
		SchematicRegistry.registerSchematicBlock(Blocks.pumpkin_stem, SchematicSeeds.class, Items.pumpkin_seeds);
		SchematicRegistry.registerSchematicBlock(Blocks.melon_stem, SchematicSeeds.class, Items.melon_seeds);
		SchematicRegistry.registerSchematicBlock(Blocks.nether_wart, SchematicSeeds.class, Items.nether_wart);

		SchematicRegistry.registerSchematicBlock(Blocks.torch, SchematicWallSide.class);
		SchematicRegistry.registerSchematicBlock(Blocks.redstone_torch, SchematicWallSide.class);
		SchematicRegistry.registerSchematicBlock(Blocks.unlit_redstone_torch, SchematicWallSide.class);

		SchematicRegistry.registerSchematicBlock(Blocks.tripwire_hook, SchematicTripWireHook.class);

		SchematicRegistry.registerSchematicBlock(Blocks.skull, SchematicSkull.class);

		SchematicRegistry.registerSchematicBlock(Blocks.ladder, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		SchematicRegistry.registerSchematicBlock(Blocks.fence_gate, SchematicRotateMeta.class, new int[]{0, 1, 2, 3}, true);
		SchematicRegistry.registerSchematicBlock(Blocks.log, SchematicRotateMeta.class, new int[]{8, 4, 8, 4}, true);
		SchematicRegistry.registerSchematicBlock(Blocks.log2, SchematicRotateMeta.class, new int[]{8, 4, 8, 4}, true);
		SchematicRegistry.registerSchematicBlock(Blocks.hay_block, SchematicRotateMeta.class, new int[]{8, 4, 8, 4}, true);
		SchematicRegistry.registerSchematicBlock(Blocks.quartz_block, SchematicRotateMeta.class, new int[]{4, 3, 4, 3}, true);
		SchematicRegistry.registerSchematicBlock(Blocks.hopper, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		SchematicRegistry.registerSchematicBlock(Blocks.anvil, SchematicRotateMeta.class, new int[]{0, 1, 2, 3}, true);

		SchematicRegistry.registerSchematicBlock(Blocks.furnace, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		SchematicRegistry.registerSchematicBlock(Blocks.lit_furnace, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		SchematicRegistry.registerSchematicBlock(Blocks.chest, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		SchematicRegistry.registerSchematicBlock(Blocks.dispenser, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		SchematicRegistry.registerSchematicBlock(Blocks.dropper, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);

		SchematicRegistry.registerSchematicBlock(Blocks.ender_chest, SchematicEnderChest.class);

		SchematicRegistry.registerSchematicBlock(Blocks.vine, SchematicRotateMeta.class, new int[]{1, 4, 8, 2}, false);
		SchematicRegistry.registerSchematicBlock(Blocks.trapdoor, SchematicRotateMeta.class, new int[]{0, 1, 2, 3}, false);

		SchematicRegistry.registerSchematicBlock(Blocks.wooden_button, SchematicLever.class);
		SchematicRegistry.registerSchematicBlock(Blocks.stone_button, SchematicLever.class);
		SchematicRegistry.registerSchematicBlock(Blocks.lever, SchematicLever.class);

		SchematicRegistry.registerSchematicBlock(Blocks.stone, SchematicStone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.gold_ore, SchematicStone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.iron_ore, SchematicStone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.coal_ore, SchematicStone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.lapis_ore, SchematicStone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.diamond_ore, SchematicStone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.redstone_ore, SchematicStone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.lit_redstone_ore, SchematicStone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.emerald_ore, SchematicStone.class);

		SchematicRegistry.registerSchematicBlock(Blocks.gravel, SchematicGravel.class);

		SchematicRegistry.registerSchematicBlock(Blocks.redstone_wire, SchematicRedstoneWire.class, new ItemStack(Items.redstone));
		SchematicRegistry.registerSchematicBlock(Blocks.cake, SchematicCustomStack.class, new ItemStack(Items.cake));
		SchematicRegistry.registerSchematicBlock(Blocks.glowstone, SchematicCustomStack.class, new ItemStack(Blocks.glowstone));

		SchematicRegistry.registerSchematicBlock(Blocks.powered_repeater, SchematicRedstoneDiode.class, Items.repeater);
		SchematicRegistry.registerSchematicBlock(Blocks.unpowered_repeater, SchematicRedstoneDiode.class, Items.repeater);
		SchematicRegistry.registerSchematicBlock(Blocks.powered_comparator, SchematicRedstoneDiode.class, Items.comparator);
		SchematicRegistry.registerSchematicBlock(Blocks.unpowered_comparator, SchematicRedstoneDiode.class, Items.comparator);

		SchematicRegistry.registerSchematicBlock(Blocks.redstone_lamp, SchematicRedstoneLamp.class);
		SchematicRegistry.registerSchematicBlock(Blocks.lit_redstone_lamp, SchematicRedstoneLamp.class);

		SchematicRegistry.registerSchematicBlock(Blocks.water, SchematicFluid.class, new ItemStack(Items.water_bucket));
		SchematicRegistry.registerSchematicBlock(Blocks.flowing_water, SchematicFluid.class, new ItemStack(Items.water_bucket));
		SchematicRegistry.registerSchematicBlock(Blocks.lava, SchematicFluid.class, new ItemStack(Items.lava_bucket));
		SchematicRegistry.registerSchematicBlock(Blocks.flowing_lava, SchematicFluid.class, new ItemStack(Items.lava_bucket));

		SchematicRegistry.registerSchematicBlock(Blocks.glass_pane, SchematicGlassPane.class);
		SchematicRegistry.registerSchematicBlock(Blocks.stained_glass_pane, SchematicGlassPane.class);

		SchematicRegistry.registerSchematicBlock(Blocks.piston, SchematicPiston.class);
		SchematicRegistry.registerSchematicBlock(Blocks.piston_extension, SchematicPiston.class);
		SchematicRegistry.registerSchematicBlock(Blocks.sticky_piston, SchematicPiston.class);

		SchematicRegistry.registerSchematicBlock(Blocks.lit_pumpkin, SchematicPumpkin.class);

		SchematicRegistry.registerSchematicBlock(Blocks.oak_stairs, SchematicStairs.class);
		SchematicRegistry.registerSchematicBlock(Blocks.stone_stairs, SchematicStairs.class);
		SchematicRegistry.registerSchematicBlock(Blocks.brick_stairs, SchematicStairs.class);
		SchematicRegistry.registerSchematicBlock(Blocks.stone_brick_stairs, SchematicStairs.class);
		SchematicRegistry.registerSchematicBlock(Blocks.nether_brick_stairs, SchematicStairs.class);
		SchematicRegistry.registerSchematicBlock(Blocks.sandstone_stairs, SchematicStairs.class);
		SchematicRegistry.registerSchematicBlock(Blocks.spruce_stairs, SchematicStairs.class);
		SchematicRegistry.registerSchematicBlock(Blocks.birch_stairs, SchematicStairs.class);
		SchematicRegistry.registerSchematicBlock(Blocks.jungle_stairs, SchematicStairs.class);
		SchematicRegistry.registerSchematicBlock(Blocks.quartz_stairs, SchematicStairs.class);
		SchematicRegistry.registerSchematicBlock(Blocks.acacia_stairs, SchematicStairs.class);
		SchematicRegistry.registerSchematicBlock(Blocks.dark_oak_stairs, SchematicStairs.class);

		SchematicRegistry.registerSchematicBlock(Blocks.wooden_door, SchematicDoor.class, new ItemStack(Items.wooden_door));
		SchematicRegistry.registerSchematicBlock(Blocks.iron_door, SchematicDoor.class, new ItemStack(Items.iron_door));

		SchematicRegistry.registerSchematicBlock(Blocks.bed, SchematicBed.class);

		SchematicRegistry.registerSchematicBlock(Blocks.wall_sign, SchematicSign.class, true);
		SchematicRegistry.registerSchematicBlock(Blocks.standing_sign, SchematicSign.class, false);

		SchematicRegistry.registerSchematicBlock(Blocks.portal, SchematicPortal.class);

		SchematicRegistry.registerSchematicBlock(Blocks.rail, SchematicRail.class);
		SchematicRegistry.registerSchematicBlock(Blocks.activator_rail, SchematicRail.class);
		SchematicRegistry.registerSchematicBlock(Blocks.detector_rail, SchematicRail.class);
		SchematicRegistry.registerSchematicBlock(Blocks.golden_rail, SchematicRail.class);

		SchematicRegistry.registerSchematicBlock(Blocks.fire, SchematicFire.class);

		SchematicRegistry.registerSchematicBlock(Blocks.bedrock, SchematicBlockCreative.class);

		SchematicRegistry.registerSchematicBlock(Blocks.mob_spawner, SchematicTileCreative.class);

		SchematicRegistry.registerSchematicBlock(Blocks.glass, SchematicStandalone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.stone_slab, SchematicStandalone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.double_stone_slab, SchematicStandalone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.wooden_slab, SchematicStandalone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.double_wooden_slab, SchematicStandalone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.stained_glass, SchematicStandalone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.fence, SchematicStandalone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.daylight_detector, SchematicStandalone.class);
		SchematicRegistry.registerSchematicBlock(Blocks.iron_bars, SchematicStandalone.class);

		// Standard entities

		SchematicRegistry.registerSchematicEntity(EntityMinecartEmpty.class, SchematicMinecart.class, Items.minecart);
		SchematicRegistry.registerSchematicEntity(EntityMinecartFurnace.class, SchematicMinecart.class, Items.furnace_minecart);
		SchematicRegistry.registerSchematicEntity(EntityMinecartTNT.class, SchematicMinecart.class, Items.tnt_minecart);
		SchematicRegistry.registerSchematicEntity(EntityMinecartChest.class, SchematicMinecart.class, Items.chest_minecart);
		SchematicRegistry.registerSchematicEntity(EntityMinecartHopper.class, SchematicMinecart.class, Items.hopper_minecart);

		SchematicRegistry.registerSchematicEntity(EntityPainting.class, SchematicHanging.class, Items.painting);
		SchematicRegistry.registerSchematicEntity(EntityItemFrame.class, SchematicHanging.class, Items.item_frame);

		// BuildCraft blocks

		SchematicRegistry.registerSchematicBlock(architectBlock, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);
		SchematicRegistry.registerSchematicBlock(builderBlock, SchematicRotateMeta.class, new int[]{2, 5, 3, 4}, true);

		SchematicRegistry.registerSchematicBlock(markerBlock, SchematicWallSide.class);
		SchematicRegistry.registerSchematicBlock(pathMarkerBlock, SchematicWallSide.class);

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
		CoreProxy.proxy.registerBlock(buildToolBlock);

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

		SchematicRegistry.readConfiguration(BuildCraftCore.mainConfiguration);

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

		ActionManager.registerActionProvider(new BuildersActionProvider());
	}

	public static void loadRecipes() {
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(templateItem, 1), "ppp", "pip", "ppp", 'i',
			new ItemStack(Items.dye, 1, 0), 'p', Items.paper);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(blueprintItem, 1), "ppp", "pip", "ppp", 'i',
			new ItemStack(Items.dye, 1, 4), 'p', Items.paper);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(markerBlock, 1), "l ", "r ", 'l',
			new ItemStack(Items.dye, 1, 4), 'r', Blocks.redstone_torch);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(pathMarkerBlock, 1), "l ", "r ", 'l',
			new ItemStack(Items.dye, 1, 2), 'r', Blocks.redstone_torch);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(fillerBlock, 1), "btb", "ycy", "gCg", 'b',
			new ItemStack(Items.dye, 1, 0), 't', markerBlock, 'y', new ItemStack(Items.dye, 1, 11),
			'c', Blocks.crafting_table, 'g', BuildCraftCore.goldGearItem, 'C', Blocks.chest);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(builderBlock, 1), "btb", "ycy", "gCg", 'b',
			new ItemStack(Items.dye, 1, 0), 't', markerBlock, 'y', new ItemStack(Items.dye, 1, 11),
			'c', Blocks.crafting_table, 'g', BuildCraftCore.diamondGearItem, 'C', Blocks.chest);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(architectBlock, 1), "btb", "ycy", "gCg", 'b',
			new ItemStack(Items.dye, 1, 0), 't', markerBlock, 'y', new ItemStack(Items.dye, 1, 11),
			'c', Blocks.crafting_table, 'g', BuildCraftCore.diamondGearItem, 'C',
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
}

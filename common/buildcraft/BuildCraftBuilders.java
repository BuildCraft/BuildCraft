/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
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
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import buildcraft.api.blueprints.BlueprintDeployer;
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.ISchematicRegistry;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.blueprints.SchematicFactory;
import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.JavaTools;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.StatementManager;
import buildcraft.builders.*;
import buildcraft.builders.blueprints.BlueprintDatabase;
import buildcraft.builders.schematics.*;
import buildcraft.builders.statements.ActionFiller;
import buildcraft.builders.statements.BuildersActionProvider;
import buildcraft.builders.urbanism.BlockUrbanist;
import buildcraft.builders.urbanism.TileUrbanist;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.blueprints.RealBlueprintDeployer;
import buildcraft.core.blueprints.SchematicRegistry;
import buildcraft.core.builders.patterns.*;
import buildcraft.core.proxy.CoreProxy;

@Mod(name = "BuildCraft Builders", version = Version.VERSION, useMetadata = false, modid = "BuildCraftBuilders",
		dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftBuilders extends BuildCraftMod {

	@Mod.Instance("BuildCraftBuilders")
	public static BuildCraftBuilders instance;

	public static final char BPT_SEP_CHARACTER = '-';
	public static final int LIBRARY_PAGE_SIZE = 12;
	public static final int MAX_BLUEPRINTS_NAME_SIZE = 32;
	/** Used for removing blocks from the world */
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
		String blueprintServerDir =
			BuildCraftCore.mainConfiguration.get(
				Configuration.CATEGORY_GENERAL,
				"blueprints.serverDir",
				"\"$MINECRAFT" + File.separator + "config" + File.separator + "buildcraft" + File.separator + "blueprints" + File.separator
					+ "server\"").getString();

		String blueprintLibraryOutput =
			BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "blueprints.libraryOutput",
				"\"$MINECRAFT" + File.separator + "blueprints\"").getString();

		String[] blueprintLibraryInput =
			BuildCraftCore.mainConfiguration.get(
				Configuration.CATEGORY_GENERAL,
				"blueprints.libraryInput",
				new String[] {
					// expected location
					"\"$MINECRAFT" + File.separator + "blueprints\"",
					// legacy beta BuildCraft
					"\"$MINECRAFT" + File.separator + "config" + File.separator + "buildcraft" + File.separator + "blueprints" + File.separator
						+ "client\"",
					// inferred user download location
					"\"" + getDownloadsDir() + "\"" }).getStringList().clone();

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

		serverDB.init(new String[] { blueprintServerDir }, blueprintServerDir);
		clientDB.init(blueprintLibraryInput, blueprintLibraryOutput);
	}

	private static String getDownloadsDir() {
		final String os = System.getProperty("os.name").toLowerCase();

		if (os.contains("nix") || os.contains("lin") || os.contains("mac")) {
			// Linux, Mac or other UNIX
			// According XDG specification every user-specified folder can be localized
			// or even moved to any destination, so we obtain real path with xdg-user-dir
			try {
				Process process = Runtime.getRuntime().exec(new String[] { "xdg-user-dir", "DOWNLOAD" });
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
				process.waitFor();
				String line = reader.readLine().trim();
				reader.close();

				if (line.length() > 0) {
					return line;
				}
			}
			catch (Exception ignored) {
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
		}
		else {
			result = result.replace("$MINECRAFT", Launch.minecraftHome.getAbsolutePath());
		}

		if ("/".equals(File.separator)) {
			result = result.replaceAll("\\\\", "/");
		}
		else {
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
			}
			catch (Exception e) {
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

		schemes.registerSchematicBlock(Blocks.torch, SchematicBlock.class);
		schemes.registerSchematicBlock(Blocks.redstone_torch, SchematicBlock.class);
		schemes.registerSchematicBlock(Blocks.unlit_redstone_torch, SchematicBlock.class);

		schemes.registerSchematicBlock(Blocks.tripwire_hook, SchematicBlock.class);

		schemes.registerSchematicBlock(Blocks.skull, SchematicSkull.class);

		schemes.registerSchematicBlock(Blocks.acacia_fence_gate, SchematicBlock.class);
		schemes.registerSchematicBlock(Blocks.birch_fence_gate, SchematicBlock.class);
		schemes.registerSchematicBlock(Blocks.dark_oak_fence_gate, SchematicBlock.class);
		schemes.registerSchematicBlock(Blocks.jungle_fence_gate, SchematicBlock.class);
		schemes.registerSchematicBlock(Blocks.oak_fence_gate, SchematicBlock.class);
		schemes.registerSchematicBlock(Blocks.spruce_fence_gate, SchematicBlock.class);

		schemes.registerSchematicBlock(Blocks.log, SchematicLog.class);
		schemes.registerSchematicBlock(Blocks.log2, SchematicLog.class);
		schemes.registerSchematicBlock(Blocks.hay_block, SchematicRotatedPillar.class);
		schemes.registerSchematicBlock(Blocks.quartz_block, SchematicQuartz.class);
		schemes.registerSchematicBlock(Blocks.hopper, SchematicTile.class);
		schemes.registerSchematicBlock(Blocks.anvil, SchematicCustomStack.class, new ItemStack(Blocks.anvil));

		schemes.registerSchematicBlock(Blocks.vine, SchematicVine.class);

		schemes.registerSchematicBlock(Blocks.furnace, SchematicTile.class);
		schemes.registerSchematicBlock(Blocks.lit_furnace, SchematicTile.class);
		schemes.registerSchematicBlock(Blocks.chest, SchematicTile.class);
		schemes.registerSchematicBlock(Blocks.dispenser, SchematicTile.class);
		schemes.registerSchematicBlock(Blocks.dropper, SchematicTile.class);

		schemes.registerSchematicBlock(Blocks.ender_chest, SchematicEnderChest.class);

		schemes.registerSchematicBlock(Blocks.lever, SchematicLever.class);

		schemes.registerSchematicBlock(Blocks.stone, SchematicStone.class);
		schemes.registerSchematicBlock(Blocks.gold_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());
		schemes.registerSchematicBlock(Blocks.iron_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());
		schemes.registerSchematicBlock(Blocks.coal_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());
		schemes.registerSchematicBlock(Blocks.lapis_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());
		schemes.registerSchematicBlock(Blocks.diamond_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());
		schemes.registerSchematicBlock(Blocks.redstone_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());
		schemes.registerSchematicBlock(Blocks.lit_redstone_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());
		schemes.registerSchematicBlock(Blocks.emerald_ore, SchematicTreatAsOther.class, Blocks.stone.getDefaultState());

		schemes.registerSchematicBlock(Blocks.gravel, SchematicGravel.class);

		schemes.registerSchematicBlock(Blocks.redstone_wire, SchematicRedstoneWire.class, new ItemStack(Items.redstone));
		schemes.registerSchematicBlock(Blocks.cake, SchematicCustomStack.class, new ItemStack(Items.cake));
		schemes.registerSchematicBlock(Blocks.glowstone, SchematicCustomStack.class, new ItemStack(Blocks.glowstone));

		schemes.registerSchematicBlock(Blocks.powered_repeater, SchematicCustomStack.class, new ItemStack(Items.repeater));
		schemes.registerSchematicBlock(Blocks.unpowered_repeater, SchematicCustomStack.class, new ItemStack(Items.repeater));
		schemes.registerSchematicBlock(Blocks.powered_comparator, SchematicCustomStack.class, new ItemStack(Items.comparator));
		schemes.registerSchematicBlock(Blocks.unpowered_comparator, SchematicCustomStack.class, new ItemStack(Items.comparator));

		schemes.registerSchematicBlock(Blocks.redstone_lamp, SchematicRedstoneLamp.class);
		schemes.registerSchematicBlock(Blocks.lit_redstone_lamp, SchematicRedstoneLamp.class);

		schemes.registerSchematicBlock(Blocks.glass_pane, SchematicGlassPane.class);
		schemes.registerSchematicBlock(Blocks.stained_glass_pane, SchematicGlassPane.class);

		schemes.registerSchematicBlock(Blocks.piston, SchematicTile.class);
		schemes.registerSchematicBlock(Blocks.piston_extension, SchematicTile.class);
		schemes.registerSchematicBlock(Blocks.sticky_piston, SchematicTile.class);

		// schemes.registerSchematicBlock(Blocks.lit_pumpkin, SchematicPumpkin.class);

		schemes.registerSchematicBlock(Blocks.oak_stairs, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.stone_stairs, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.brick_stairs, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.stone_brick_stairs, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.nether_brick_stairs, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.sandstone_stairs, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.spruce_stairs, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.birch_stairs, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.jungle_stairs, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.quartz_stairs, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.acacia_stairs, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.dark_oak_stairs, SchematicStandalone.class);

		schemes.registerSchematicBlock(Blocks.acacia_door, SchematicDoor.class, new ItemStack(Items.acacia_door));
		schemes.registerSchematicBlock(Blocks.birch_door, SchematicDoor.class, new ItemStack(Items.birch_door));
		schemes.registerSchematicBlock(Blocks.dark_oak_door, SchematicDoor.class, new ItemStack(Items.dark_oak_door));
		schemes.registerSchematicBlock(Blocks.jungle_door, SchematicDoor.class, new ItemStack(Items.jungle_door));
		schemes.registerSchematicBlock(Blocks.oak_door, SchematicDoor.class, new ItemStack(Items.oak_door));
		schemes.registerSchematicBlock(Blocks.spruce_door, SchematicDoor.class, new ItemStack(Items.spruce_door));
		schemes.registerSchematicBlock(Blocks.iron_door, SchematicDoor.class, new ItemStack(Items.iron_door));

		schemes.registerSchematicBlock(Blocks.bed, SchematicBed.class);

		schemes.registerSchematicBlock(Blocks.wall_sign, SchematicTile.class);
		schemes.registerSchematicBlock(Blocks.standing_sign, SchematicStandingSign.class);

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
		schemes.registerSchematicBlock(Blocks.acacia_fence, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.birch_fence, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.dark_oak_fence, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.jungle_fence, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.oak_fence, SchematicStandalone.class);
		schemes.registerSchematicBlock(Blocks.spruce_fence, SchematicStandalone.class);
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

		schemes.registerSchematicBlock(architectBlock, SchematicTile.class);
		schemes.registerSchematicBlock(builderBlock, SchematicTile.class);

		// schemes.registerSchematicBlock(markerBlock, SchematicWallSide.class);
		// schemes.registerSchematicBlock(pathMarkerBlock, SchematicWallSide.class);
		// schemes.registerSchematicBlock(constructionMarkerBlock, SchematicWallSide.class);

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

		buildToolBlock = new BlockBuildTool();
		buildToolBlock.setUnlocalizedName("buildToolBlock");
		CoreProxy.proxy.registerBlock(buildToolBlock);

		markerBlock = new BlockMarker();
		CoreProxy.proxy.registerBlock(markerBlock.setUnlocalizedName("markerBlock"));

		pathMarkerBlock = new BlockPathMarker();
		CoreProxy.proxy.registerBlock(pathMarkerBlock.setUnlocalizedName("pathMarkerBlock"));

		constructionMarkerBlock = new BlockConstructionMarker();
		CoreProxy.proxy.registerBlock(constructionMarkerBlock.setUnlocalizedName("constructionMarkerBlock"), ItemConstructionMarker.class);

		fillerBlock = new BlockFiller();
		CoreProxy.proxy.registerBlock(fillerBlock.setUnlocalizedName("fillerBlock"));

		builderBlock = new BlockBuilder();
		CoreProxy.proxy.registerBlock(builderBlock.setUnlocalizedName("builderBlock"));

		architectBlock = new BlockArchitect();
		CoreProxy.proxy.registerBlock(architectBlock.setUnlocalizedName("architectBlock"));

		libraryBlock = new BlockBlueprintLibrary();
		CoreProxy.proxy.registerBlock(libraryBlock.setUnlocalizedName("libraryBlock"));

		if (!BuildCraftCore.NONRELEASED_BLOCKS) {
			urbanistBlock = new BlockUrbanist();
			CoreProxy.proxy.registerBlock(urbanistBlock.setUnlocalizedName("urbanistBlock"));
			CoreProxy.proxy.registerTileEntity(TileUrbanist.class, "net.minecraft.src.builders.TileUrbanist");
		}

		GameRegistry.registerTileEntity(TileMarker.class, "Marker");
		GameRegistry.registerTileEntity(TileFiller.class, "Filler");
		GameRegistry.registerTileEntity(TileBuilder.class, "net.minecraft.src.builders.TileBuilder");
		GameRegistry.registerTileEntity(TileArchitect.class, "net.minecraft.src.builders.TileTemplate");
		GameRegistry.registerTileEntity(TilePathMarker.class, "net.minecraft.src.builders.TilePathMarker");
		GameRegistry.registerTileEntity(TileConstructionMarker.class, "net.minecraft.src.builders.TileConstructionMarker");
		GameRegistry.registerTileEntity(TileBlueprintLibrary.class, "net.minecraft.src.builders.TileBlueprintLibrary");

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
		}
		catch (Error error) {
			BCLog.logErrorAPI("Buildcraft", error, IFillerPattern.class);
			throw error;
		}

		StatementManager.registerActionProvider(new BuildersActionProvider());
	}

	public static void loadRecipes() {
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(templateItem, 1), "ppp", "pip", "ppp", 'i', "dyeBlack", 'p', Items.paper);

		CoreProxy.proxy
			.addCraftingRecipe(new ItemStack(blueprintItem, 1), "ppp", "pip", "ppp", 'i', new ItemStack(Items.dye, 1, 4), 'p', Items.paper);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(markerBlock, 1), "l ", "r ", 'l', new ItemStack(Items.dye, 1, 4), 'r', Blocks.redstone_torch);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(pathMarkerBlock, 1), "l ", "r ", 'l', "dyeGreen", 'r', Blocks.redstone_torch);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(fillerBlock, 1), "btb", "ycy", "gCg", 'b', "dyeBlack", 't', markerBlock, 'y', "dyeYellow",
			'c', Blocks.crafting_table, 'g', "gearGold", 'C', Blocks.chest);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(builderBlock, 1), "btb", "ycy", "gCg", 'b', "dyeBlack", 't', markerBlock, 'y', "dyeYellow",
			'c', Blocks.crafting_table, 'g', "gearDiamond", 'C', Blocks.chest);

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(architectBlock, 1), "btb", "ycy", "gCg", 'b', "dyeBlack", 't', markerBlock, 'y', "dyeYellow",
			'c', Blocks.crafting_table, 'g', "gearDiamond", 'C', new ItemStack(blueprintItem, 1));

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(libraryBlock, 1), "bbb", "bBb", "bbb", 'b', new ItemStack(blueprintItem), 'B',
			Blocks.bookshelf);
	}

	@Mod.EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}

	@Mod.EventHandler
	public void serverStop(FMLServerStoppingEvent event) {
		TilePathMarker.clearAvailableMarkersList();
	}

	/* @SubscribeEvent
	 * @SideOnly(Side.CLIENT) public void loadTextures(TextureStitchEvent.Pre evt) { if (evt.map.getTextureType() == 0)
	 * { for (FillerPattern pattern : FillerPattern.patterns.values()) { pattern.registerIcon(evt.map); } } }
	 * @SubscribeEvent
	 * @SideOnly(Side.CLIENT) public void textureHook(TextureStitchEvent.Pre event) { if (event.map.getTextureType() ==
	 * 1) { UrbanistToolsIconProvider.INSTANCE.registerIcons(event.map); } } */

	@Mod.EventHandler
	public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
		// FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
		// TileMarker.class.getCanonicalName());
		// FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
		// TileFiller.class.getCanonicalName());
		// FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
		// TileBuilder.class.getCanonicalName());
		// FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
		// TileArchitect.class.getCanonicalName());
		// FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial",
		// TilePathMarker.class.getCanonicalName());
		FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial", TileBlueprintLibrary.class.getCanonicalName());
	}
}

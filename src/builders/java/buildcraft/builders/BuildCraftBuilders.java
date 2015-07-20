/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
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
import net.minecraft.stats.Achievement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.blueprints.BlueprintDeployer;
import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.ISchematicRegistry;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.blueprints.SchematicFactory;
import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.ConfigAccessor;
import buildcraft.api.core.ConfigAccessor.EMod;
import buildcraft.api.core.JavaTools;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.library.LibraryAPI;
import buildcraft.api.statements.StatementManager;
import buildcraft.builders.block.BlockArchitect;
import buildcraft.builders.block.BlockBlueprintLibrary;
import buildcraft.builders.block.BlockBuilder;
import buildcraft.builders.block.BlockConstructionMarker;
import buildcraft.builders.block.BlockFiller;
import buildcraft.builders.block.BlockFrame;
import buildcraft.builders.block.BlockMarker;
import buildcraft.builders.block.BlockPathMarker;
import buildcraft.builders.block.BlockQuarry;
import buildcraft.builders.blueprints.RealBlueprintDeployer;
import buildcraft.builders.item.ItemBlueprintStandard;
import buildcraft.builders.item.ItemBlueprintTemplate;
import buildcraft.builders.item.ItemConstructionMarker;
import buildcraft.builders.schematics.*;
import buildcraft.builders.statements.BuildersActionProvider;
import buildcraft.builders.tile.TileArchitect;
import buildcraft.builders.tile.TileBlueprintLibrary;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileConstructionMarker;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TileMarker;
import buildcraft.builders.tile.TilePathMarker;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.builders.urbanism.BlockUrbanist;
import buildcraft.builders.urbanism.TileUrbanist;
import buildcraft.builders.urbanism.UrbanistToolsIconProvider;
import buildcraft.core.BuildCraftCore;
import buildcraft.core.BuildCraftMod;
import buildcraft.core.CompatHooks;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.Version;
import buildcraft.core.blueprints.SchematicRegistry;
import buildcraft.core.builders.patterns.*;
import buildcraft.core.builders.schematics.SchematicBlockCreative;
import buildcraft.core.builders.schematics.SchematicIgnore;
import buildcraft.core.builders.schematics.SchematicStandalone;
import buildcraft.core.builders.schematics.SchematicTileCreative;
import buildcraft.core.config.ConfigManager;
import buildcraft.core.proxy.CoreProxy;

@Mod(name = "BuildCraft Builders", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Builders",
        dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftBuilders extends BuildCraftMod {

    @Mod.Instance("BuildCraft|Builders")
    public static BuildCraftBuilders instance;

    public static BlockMarker markerBlock;
    public static BlockPathMarker pathMarkerBlock;
    public static BlockConstructionMarker constructionMarkerBlock;
    public static BlockFiller fillerBlock;
    public static BlockBuilder builderBlock;
    public static BlockArchitect architectBlock;
    public static BlockBlueprintLibrary libraryBlock;
    public static BlockUrbanist urbanistBlock;
    public static BlockQuarry quarryBlock;
    public static BlockFrame frameBlock;
    public static ItemBlueprintTemplate templateItem;
    public static ItemBlueprintStandard blueprintItem;

    public static Achievement architectAchievement;
    public static Achievement libraryAchievement;
    public static Achievement blueprintAchievement;
    public static Achievement builderAchievement;
    public static Achievement templateAchievement;
    public static Achievement chunkDestroyerAchievement;

    public static BlueprintServerDatabase serverDB;
    public static LibraryDatabase clientDB;

    public static boolean debugPrintSchematicList = false;
    public static boolean dropBrokenBlocks = false;

    public static boolean quarryLoadsChunks = true;
    public static boolean quarryOneTimeUse = false;

    private String blueprintServerDir, blueprintClientDir;

    public class QuarryChunkloadCallback implements ForgeChunkManager.OrderedLoadingCallback {
        @Override
        public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
            for (ForgeChunkManager.Ticket ticket : tickets) {
                int quarryX = ticket.getModData().getInteger("quarryX");
                int quarryY = ticket.getModData().getInteger("quarryY");
                int quarryZ = ticket.getModData().getInteger("quarryZ");
                BlockPos pos = new BlockPos(quarryX, quarryY, quarryZ);

                Block block = world.getBlockState(pos).getBlock();
                if (block == quarryBlock) {
                    TileQuarry tq = (TileQuarry) world.getTileEntity(pos);
                    tq.forceChunkLoading(ticket);
                }
            }
        }

        @Override
        public List<ForgeChunkManager.Ticket> ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world, int maxTicketCount) {
            List<ForgeChunkManager.Ticket> validTickets = Lists.newArrayList();
            for (ForgeChunkManager.Ticket ticket : tickets) {
                int quarryX = ticket.getModData().getInteger("quarryX");
                int quarryY = ticket.getModData().getInteger("quarryY");
                int quarryZ = ticket.getModData().getInteger("quarryZ");
                BlockPos pos = new BlockPos(quarryX, quarryY, quarryZ);

                Block block = world.getBlockState(pos).getBlock();
                if (block == quarryBlock) {
                    validTickets.add(ticket);
                }
            }
            return validTickets;
        }
    }

    @Mod.EventHandler
    public void loadConfiguration(FMLPreInitializationEvent evt) {
        ConfigAccessor.addMod(EMod.BUILDERS, this);

        BuildCraftCore.mainConfigManager.register("blueprints.serverDatabaseDirectory", "\"$MINECRAFT" + File.separator + "config" + File.separator
            + "buildcraft" + File.separator + "blueprints" + File.separator + "server\"",
                "Location for the server blueprint database (used by all blueprint items).", ConfigManager.RestartRequirement.WORLD);
        BuildCraftCore.mainConfigManager.register("blueprints.clientDatabaseDirectory", "\"$MINECRAFT" + File.separator + "blueprints\"",
                "Location for the client blueprint database (used by the Electronic Library).", ConfigManager.RestartRequirement.NONE);

        BuildCraftCore.mainConfigManager.register("general.markerRange", 64, "Set the maximum marker range.", ConfigManager.RestartRequirement.NONE);
        BuildCraftCore.mainConfigManager.register("general.quarry.oneTimeUse", false, "Should the quarry only be usable once after placing?",
                ConfigManager.RestartRequirement.NONE);
        BuildCraftCore.mainConfigManager.register("general.quarry.doChunkLoading", true, "Should the quarry keep the chunks it is working on loaded?",
                ConfigManager.RestartRequirement.NONE);

        BuildCraftCore.mainConfigManager.register("builders.dropBrokenBlocks", false, "Should the builder and filler drop the cleared blocks?",
                ConfigManager.RestartRequirement.NONE);

        BuildCraftCore.mainConfigManager.get("blueprints.serverDatabaseDirectory").setShowInGui(false);
        BuildCraftCore.mainConfigManager.get("general.markerRange").setMinValue(8).setMaxValue(64);

        serverDB = new BlueprintServerDatabase();
        clientDB = new LibraryDatabase();

        reloadConfig(ConfigManager.RestartRequirement.GAME);

        // TODO
        // Property dropBlock = BuildCraftCore.mainConfiguration.get("general", "builder.dropBrokenBlocks", false,
        // "set to true to force the builder to drop broken blocks");
        // dropBrokenBlocks = dropBlock.getBoolean(false);

        Property printSchematicList = BuildCraftCore.mainConfiguration.get("debug", "printBlueprintSchematicList", false);
        debugPrintSchematicList = printSchematicList.getBoolean();
    }

    public void reloadConfig(ConfigManager.RestartRequirement restartType) {
        if (restartType == ConfigManager.RestartRequirement.GAME) {

            reloadConfig(ConfigManager.RestartRequirement.WORLD);
        } else if (restartType == ConfigManager.RestartRequirement.WORLD) {
            blueprintServerDir = BuildCraftCore.mainConfigManager.get("blueprints.serverDatabaseDirectory").getString();
            blueprintServerDir = JavaTools.stripSurroundingQuotes(replacePathVariables(blueprintServerDir));
            serverDB.init(new String[] { blueprintServerDir }, blueprintServerDir);

            reloadConfig(ConfigManager.RestartRequirement.NONE);
        } else {
            quarryOneTimeUse = BuildCraftCore.mainConfigManager.get("general.quarry.oneTimeUse").getBoolean();
            quarryLoadsChunks = BuildCraftCore.mainConfigManager.get("general.quarry.doChunkLoading").getBoolean();

            blueprintClientDir = BuildCraftCore.mainConfigManager.get("blueprints.clientDatabaseDirectory").getString();
            blueprintClientDir = JavaTools.stripSurroundingQuotes(replacePathVariables(blueprintClientDir));
            clientDB.init(new String[] { blueprintClientDir, getDownloadsDir() }, blueprintClientDir);

            DefaultProps.MARKER_RANGE = BuildCraftCore.mainConfigManager.get("general.markerRange").getInt();

            dropBrokenBlocks = BuildCraftCore.mainConfigManager.get("builders.dropBrokenBlocks").getBoolean();

            if (BuildCraftCore.mainConfiguration.hasChanged()) {
                BuildCraftCore.mainConfiguration.save();
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent event) {
        if ("BuildCraftCore".equals(event.modID)) {
            reloadConfig(event.isWorldRunning ? ConfigManager.RestartRequirement.NONE : ConfigManager.RestartRequirement.WORLD);
        }
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
        ForgeChunkManager.setForcedChunkLoadingCallback(instance, new QuarryChunkloadCallback());

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

        // Refresh the databases once all the library type handlers are registered
        serverDB.refresh();
        clientDB.refresh();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        // Register gui handler
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new BuildersGuiHandler());

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

        LibraryAPI.registerHandler(new LibraryBlueprintTypeHandler(false)); // Template
        LibraryAPI.registerHandler(new LibraryBlueprintTypeHandler(true)); // Blueprint
        LibraryAPI.registerHandler(new LibraryBookTypeHandler());

        BlueprintDeployer.instance = new RealBlueprintDeployer();

        architectAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.architect", "architectAchievement",
                11, 2, BuildCraftBuilders.architectBlock, BuildCraftCore.goldGearAchievement));
        builderAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.builder", "builderAchievement", 13, 2,
                BuildCraftBuilders.builderBlock, architectAchievement));
        blueprintAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.blueprint", "blueprintAchievement",
                11, 4, BuildCraftBuilders.blueprintItem, architectAchievement));
        templateAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.template", "templateAchievement", 13,
                4, BuildCraftBuilders.templateItem, blueprintAchievement));
        libraryAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.blueprintLibrary",
                "blueprintLibraryAchievement", 15, 2, BuildCraftBuilders.libraryBlock, builderAchievement));
        chunkDestroyerAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement("achievement.chunkDestroyer",
                "chunkDestroyerAchievement", 9, 2, quarryBlock, BuildCraftCore.diamondGearAchievement));

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

        quarryBlock = (BlockQuarry) CompatHooks.INSTANCE.getBlock(BlockQuarry.class);
        CoreProxy.proxy.registerBlock(quarryBlock.setUnlocalizedName("quarryBlock"));

        markerBlock = (BlockMarker) CompatHooks.INSTANCE.getBlock(BlockMarker.class);
        CoreProxy.proxy.registerBlock(markerBlock.setUnlocalizedName("markerBlock"));

        pathMarkerBlock = (BlockPathMarker) CompatHooks.INSTANCE.getBlock(BlockPathMarker.class);
        CoreProxy.proxy.registerBlock(pathMarkerBlock.setUnlocalizedName("pathMarkerBlock"));

        constructionMarkerBlock = (BlockConstructionMarker) CompatHooks.INSTANCE.getBlock(BlockConstructionMarker.class);
        CoreProxy.proxy.registerBlock(constructionMarkerBlock.setUnlocalizedName("constructionMarkerBlock"), ItemConstructionMarker.class);

        fillerBlock = (BlockFiller) CompatHooks.INSTANCE.getBlock(BlockFiller.class);
        CoreProxy.proxy.registerBlock(fillerBlock.setUnlocalizedName("fillerBlock"));

        frameBlock = new BlockFrame();
        CoreProxy.proxy.registerBlock(frameBlock.setUnlocalizedName("frameBlock"));

        builderBlock = (BlockBuilder) CompatHooks.INSTANCE.getBlock(BlockBuilder.class);
        CoreProxy.proxy.registerBlock(builderBlock.setUnlocalizedName("builderBlock"));

        architectBlock = (BlockArchitect) CompatHooks.INSTANCE.getBlock(BlockArchitect.class);
        CoreProxy.proxy.registerBlock(architectBlock.setUnlocalizedName("architectBlock"));

        libraryBlock = (BlockBlueprintLibrary) CompatHooks.INSTANCE.getBlock(BlockBlueprintLibrary.class);
        CoreProxy.proxy.registerBlock(libraryBlock.setUnlocalizedName("libraryBlock"));

        if (!BuildCraftCore.NONRELEASED_BLOCKS) {
            urbanistBlock = new BlockUrbanist();
            CoreProxy.proxy.registerBlock(urbanistBlock.setUnlocalizedName("urbanistBlock"));
            CoreProxy.proxy.registerTileEntity(TileUrbanist.class, "buildcraft.builders.Urbanist", "net.minecraft.src.builders.TileUrbanist");
        }

        // 1.7.10 migration code- the alternative tile entities should be removed at some point, probably when we
        // abandon 1.7.10 and move to a new major release in 1.8+
        CoreProxy.proxy.registerTileEntity(TileQuarry.class, "buildcraft.builders.Quarry", "Machine");
        CoreProxy.proxy.registerTileEntity(TileMarker.class, "buildcraft.builders.Marker", "Marker");
        CoreProxy.proxy.registerTileEntity(TileFiller.class, "buildcraft.builders.Filler", "Filler");
        CoreProxy.proxy.registerTileEntity(TileBuilder.class, "buildcraft.builders.Builder", "net.minecraft.src.builders.TileBuilder");
        CoreProxy.proxy.registerTileEntity(TileArchitect.class, "buildcraft.builders.Architect", "net.minecraft.src.builders.TileTemplate");
        CoreProxy.proxy.registerTileEntity(TilePathMarker.class, "buildcraft.builders.PathMarker", "net.minecraft.src.builders.TilePathMarker");
        CoreProxy.proxy.registerTileEntity(TileConstructionMarker.class, "buildcraft.builders.ConstructionMarker",
                "net.minecraft.src.builders.TileConstructionMarker");
        CoreProxy.proxy.registerTileEntity(TileBlueprintLibrary.class, "buildcraft.builders.BlueprintLibrary",
                "net.minecraft.src.builders.TileBlueprintLibrary");

        SchematicRegistry.INSTANCE.readConfiguration(BuildCraftCore.mainConfiguration);

        if (BuildCraftCore.mainConfiguration.hasChanged()) {
            BuildCraftCore.mainConfiguration.save();
        }

        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);

        // Create filler registry
        try {
            FillerManager.registry = new FillerRegistry();

            // INIT FILLER PATTERNS
            FillerManager.registry.addPattern(PatternNone.INSTANCE);
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
        CoreProxy.proxy.addCraftingRecipe(new ItemStack(quarryBlock), "ipi", "gig", "dDd", 'i', "gearIron", 'p', "dustRedstone", 'g', "gearGold", 'd',
                "gearDiamond", 'D', Items.diamond_pickaxe);

        CoreProxy.proxy.addCraftingRecipe(new ItemStack(templateItem, 1), "ppp", "pip", "ppp", 'i', "dyeBlack", 'p', Items.paper);

        CoreProxy.proxy.addCraftingRecipe(new ItemStack(blueprintItem, 1), "ppp", "pip", "ppp", 'i', new ItemStack(Items.dye, 1, 4), 'p',
                Items.paper);

        CoreProxy.proxy.addCraftingRecipe(new ItemStack(markerBlock, 1), "l ", "r ", 'l', new ItemStack(Items.dye, 1, 4), 'r', Blocks.redstone_torch);

        CoreProxy.proxy.addCraftingRecipe(new ItemStack(pathMarkerBlock, 1), "l ", "r ", 'l', "dyeGreen", 'r', Blocks.redstone_torch);

        CoreProxy.proxy.addCraftingRecipe(new ItemStack(constructionMarkerBlock, 1), "l ", "r ", 'l', "gearGold", 'r', Blocks.redstone_torch);

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

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void loadTextures(TextureStitchEvent.Pre evt) {
        TextureMap terrainTextures = evt.map;
        BuilderProxyClient.drillTexture = terrainTextures.registerSprite(new ResourceLocation("buildcraftbuilders:blocks/quarry/drill"));
        BuilderProxyClient.drillHeadTexture = terrainTextures.registerSprite(new ResourceLocation("buildcraftbuilders:blocks/quarry/drill_head"));
        UrbanistToolsIconProvider.INSTANCE.registerSprites(terrainTextures);
    }

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

    @Mod.EventHandler
    public void remap(FMLMissingMappingsEvent event) {
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.get()) {
            if (mapping.name.equals("BuildCraftBuilders:buildToolBlock") || mapping.name.equals("BuildCraftBuilders:null")) {
                if (mapping.type == GameRegistry.Type.ITEM) {
                    mapping.remap(Item.getItemFromBlock(BuildCraftCore.buildToolBlock));
                } else {
                    mapping.remap(BuildCraftCore.buildToolBlock);
                }
            }
        }
    }
}

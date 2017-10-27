/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.EnumColor;
import buildcraft.api.core.IWorldProperty;
import buildcraft.api.crops.CropManager;
import buildcraft.api.enums.EnumSpring;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.lists.ListRegistry;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.statements.*;
import buildcraft.api.tablet.TabletAPI;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.builders.statements.ActionFiller;
import buildcraft.core.*;
import buildcraft.core.block.BlockDecoration;
import buildcraft.core.block.BlockSpring;
import buildcraft.core.blueprints.BuildingSlotMapIterator;
import buildcraft.core.blueprints.SchematicHelper;
import buildcraft.core.blueprints.SchematicRegistry;
import buildcraft.core.builders.patterns.*;
import buildcraft.core.builders.schematics.SchematicIgnore;
import buildcraft.core.client.ConficGuiFactoryBC;
import buildcraft.core.client.CoreIconProvider;
import buildcraft.core.command.SubCommandDeop;
import buildcraft.core.command.SubCommandOp;
import buildcraft.core.crops.CropHandlerPlantable;
import buildcraft.core.crops.CropHandlerReeds;
import buildcraft.core.gen.SpringPopulate;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.block.IAdditionalDataTile;
import buildcraft.core.lib.commands.RootCommand;
import buildcraft.core.lib.engines.ItemEngine;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.core.lib.fluids.BucketHandler;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.network.base.ChannelHandler;
import buildcraft.core.lib.network.base.PacketHandler;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.list.ListTooltipHandler;
import buildcraft.core.network.EntityIds;
import buildcraft.core.properties.*;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.core.recipes.IntegrationRecipeManager;
import buildcraft.core.recipes.ProgrammingRecipeManager;
import buildcraft.core.recipes.RefineryRecipeManager;
import buildcraft.core.render.BlockHighlightHandler;
import buildcraft.core.statements.*;
import buildcraft.core.tablet.ItemTablet;
import buildcraft.core.tablet.PacketTabletMessage;
import buildcraft.core.tablet.TabletProgramMenuFactory;
import buildcraft.core.tablet.manager.TabletManagerClient;
import buildcraft.core.tablet.manager.TabletManagerServer;
import buildcraft.lib.config.FileConfigManager;
import buildcraft.lib.config.RestartRequirement;
import buildcraft.lib.list.*;
import buildcraft.lib.misc.DebuggingTools;
import buildcraft.lib.misc.data.XorShift128Random;

//@Mod(name = "BuildCraft", version = DefaultProps.VERSION, useMetadata = false, modid = "BuildCraft|Core", acceptedMinecraftVersions = "[1.8.9]",
//        dependencies = "required-after:Forge@[11.15.1.1764,11.16)", guiFactory = "buildcraft.core.config.ConfigManager",
//        updateJSON = DefaultProps.UPDATE_JSON)
@Deprecated
public class BuildCraftCore extends BuildCraftMod {
//    @Mod.Instance("BuildCraft|Core")
    public static BuildCraftCore instance;

    public static final boolean DEVELOPER_MODE = DefaultProps.VERSION.contains("@");

    public enum RenderMode {
        Full,
        NoDynamic
    }

    public static RootCommand commandBuildcraft = new RootCommand("buildcraft");
    public static XorShift128Random random = new XorShift128Random();
    public static RenderMode render = RenderMode.Full;
    public static boolean debugWorldgen = false;
    public static boolean modifyWorld = false;
    public static boolean colorBlindMode = false;
    public static boolean hidePowerNumbers = false;
    public static boolean hideFluidNumbers = false;
    public static boolean canEnginesExplode = false;
    public static boolean useServerDataOnClient = true;
    public static int itemLifespan = 1200;
    public static int updateFactor = 10;
    public static int builderMaxPerItemFactor = 1024;
    public static int longUpdateFactor = 40;
    public static Configuration mainConfiguration;
    public static ConficGuiFactoryBC mainConfigManager;
    public static FileConfigManager detailedConfigManager;

    public static BlockEngine engineBlock;
    public static BlockSpring springBlock;
    public static BlockDecoration decoratedBlock;
    public static BlockMarker markerBlock;
    public static BlockPathMarker pathMarkerBlock;
    public static Item woodenGearItem;
    public static Item stoneGearItem;
    public static Item ironGearItem;
    public static Item goldGearItem;
    public static Item diamondGearItem;
    public static Item wrenchItem;
    public static Item mapLocationItem;
    public static Item debuggerItem;
    public static ItemPaintbrush paintbrushItem;
    public static ItemList listItem;
    public static ItemTablet tabletItem;

    public static ITriggerExternal triggerMachineActive = new TriggerMachine(true);
    public static ITriggerExternal triggerMachineInactive = new TriggerMachine(false);
    public static IStatement triggerEnergyHigh = new TriggerPower(true);
    public static IStatement triggerEnergyLow = new TriggerPower(false);
    public static ITriggerExternal triggerEmptyInventory = new TriggerInventory(TriggerInventory.State.Empty);
    public static ITriggerExternal triggerContainsInventory = new TriggerInventory(TriggerInventory.State.Contains);
    public static ITriggerExternal triggerSpaceInventory = new TriggerInventory(TriggerInventory.State.Space);
    public static ITriggerExternal triggerFullInventory = new TriggerInventory(TriggerInventory.State.Full);
    public static ITriggerExternal triggerEmptyFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Empty);
    public static ITriggerExternal triggerContainsFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Contains);
    public static ITriggerExternal triggerSpaceFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Space);
    public static ITriggerExternal triggerFullFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Full);
    public static ITriggerInternal triggerRedstoneActive = new TriggerRedstoneInput(true);
    public static ITriggerInternal triggerRedstoneInactive = new TriggerRedstoneInput(false);
    public static ITriggerExternal triggerInventoryBelow25 = new TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW25);
    public static ITriggerExternal triggerInventoryBelow50 = new TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW50);
    public static ITriggerExternal triggerInventoryBelow75 = new TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW75);
    public static ITriggerExternal triggerFluidContainerBelow25 = new TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType.BELOW25);
    public static ITriggerExternal triggerFluidContainerBelow50 = new TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType.BELOW50);
    public static ITriggerExternal triggerFluidContainerBelow75 = new TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType.BELOW75);
    public static IActionInternal actionRedstone = new ActionRedstoneOutput();
    public static IActionExternal[] actionControl;

    public static boolean loadDefaultRecipes = true;
    public static boolean consumeWaterSources = false;
    public static boolean miningAllowPlayerProtectedBlocks = false;
    public static float miningMultiplier;

    public static AchievementPageManager achievementManager;

    public static Achievement woodenGearAchievement;
    public static Achievement stoneGearAchievement;
    public static Achievement ironGearAchievement;
    public static Achievement goldGearAchievement;
    public static Achievement diamondGearAchievement;
    public static Achievement wrenchAchievement;
    public static Achievement engineRedstoneAchievement;

    public static GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("buildcraft.core".getBytes()), "[BuildCraft]");

    private PacketHandler handler;

    @Mod.EventHandler
    public void loadConfiguration(FMLPreInitializationEvent evt) {
        BCLog.logger.info("Starting BuildCraft " + DefaultProps.VERSION);
        BCLog.logger.info("Copyright (c) the BuildCraft team, 2011-2016");
        BCLog.logger.info("http://www.mod-buildcraft.com");

        new BCCreativeTab("main");

        commandBuildcraft.addAlias("bc");
        commandBuildcraft.addChildCommand(new SubCommandDeop());
        commandBuildcraft.addChildCommand(new SubCommandOp());

        BuildcraftRecipeRegistry.assemblyTable = AssemblyRecipeManager.INSTANCE;
        BuildcraftRecipeRegistry.integrationTable = IntegrationRecipeManager.INSTANCE;
        BuildcraftRecipeRegistry.refinery = RefineryRecipeManager.INSTANCE;
        BuildcraftRecipeRegistry.programmingTable = ProgrammingRecipeManager.INSTANCE;

        BuilderAPI.schematicHelper = SchematicHelper.INSTANCE;
        BuilderAPI.schematicRegistry = SchematicRegistry.INSTANCE;

        File cfgBase = new File(evt.getModConfigurationDirectory(), "buildcraft");

        BCRegistry.INSTANCE.setRegistryConfig(new File(cfgBase, "objects.cfg"));
        mainConfiguration = new Configuration(new File(cfgBase, "main.cfg"));

        detailedConfigManager = new FileConfigManager(
                " The buildcraft detailed configuration file. This contains a lot of miscelaneous options that have no "
                    + "affect on gameplay.\n You should refer to the BC source code for a detailed description of what these do. "
                    + "(https://github.com/BuildCraft/BuildCraft)\n"
                    + " This file will be overwritten every time that buildcraft starts, so there is no point in adding comments");
        detailedConfigManager.setConfigFile(new File(cfgBase, "detailed.properties"));

        ConficGuiFactoryBC.setConfig(mainConfiguration);
        mainConfiguration.load();

        ConficGuiFactoryBC.getCat("debug").setShowInGui(false);
        ConficGuiFactoryBC.getCat("vars").setShowInGui(false);

        ConficGuiFactoryBC.register("general.useServerDataOnClient", BuildCraftCore.useServerDataOnClient,
                "Allows BuildCraft to use the integrated server's data on the client on singleplayer worlds. Disable if you're getting the odd crash caused by it.",
                RestartRequirement.NONE);
        ConficGuiFactoryBC.register("general.builderMaxIterationsPerItemFactor", BuildCraftCore.builderMaxPerItemFactor,
                "Lower this number if BuildCraft builders/fillers are causing TPS lag. Raise it if you think they are being too slow.",
                RestartRequirement.NONE);

        ConficGuiFactoryBC.register("general.miningBreaksPlayerProtectedBlocks", false,
                "Should BuildCraft miners be allowed to break blocks using player-specific protection?", RestartRequirement.NONE);
        ConficGuiFactoryBC.register("general.updateCheck", true, "Should I check the BuildCraft version on startup?",
                RestartRequirement.NONE);
        ConficGuiFactoryBC.register("display.hidePowerValues", false, "Should all power values (RF, RF/t) be hidden?",
                RestartRequirement.NONE);
        ConficGuiFactoryBC.register("display.hideFluidValues", false, "Should all fluid values (mB, mB/t) be hidden?",
                RestartRequirement.NONE);
        ConficGuiFactoryBC.register("general.itemLifespan", 60, "How long, in seconds, should items stay on the ground? (Vanilla = 300, default = 60)",
                RestartRequirement.NONE).setMinValue(5);
        ConficGuiFactoryBC.register("network.updateFactor", 10,
                "How often, in ticks, should network update packets be sent? Increasing this might help network performance.",
                RestartRequirement.GAME).setMinValue(1);
        ConficGuiFactoryBC.register("network.longUpdateFactor", 40,
                "How often, in ticks, should full network sync packets be sent? Increasing this might help network performance.",
                RestartRequirement.GAME).setMinValue(1);
        ConficGuiFactoryBC.register("general.canEnginesExplode", false, "Should engines explode upon overheat?",
                RestartRequirement.NONE);
        ConficGuiFactoryBC.register("worldgen.enable", true, "Should BuildCraft generate anything in the world?",
                RestartRequirement.GAME);
        ConficGuiFactoryBC.register("general.pumpsConsumeWater", false, "Should pumps consume water? Enabling this might cause performance issues!",
                RestartRequirement.NONE);
        ConficGuiFactoryBC.register("power.miningUsageMultiplier", 1.0D, "What should the multiplier of all mining-related power usage be?",
                RestartRequirement.NONE);
        ConficGuiFactoryBC.register("display.colorBlindMode", false, "Should I enable colorblind mode?", RestartRequirement.GAME);
        ConficGuiFactoryBC.register("worldgen.generateWaterSprings", true, "Should BuildCraft generate water springs?",
                RestartRequirement.GAME);

        ConficGuiFactoryBC.register("debug.network.stats", false, "Should all network packets be tracked for statistical purposes?",
                RestartRequirement.NONE);

        reloadConfig(RestartRequirement.GAME);

        wrenchItem = (new ItemWrench()).setTextureLocation("buildcraftcore:wrench").setUnlocalizedName("wrenchItem");
        BCRegistry.INSTANCE.registerItem(wrenchItem, false);

        mapLocationItem = (new ItemMapLocation()).setUnlocalizedName("mapLocation");
        BCRegistry.INSTANCE.registerItem(mapLocationItem, false);

        listItem = (ItemList) (new ItemList()).setUnlocalizedName("list");
        BCRegistry.INSTANCE.registerItem(listItem, false);

        debuggerItem = (new ItemDebugger()).setUnlocalizedName("debugger");
        BCRegistry.INSTANCE.registerItem(debuggerItem, false);

        if (BuildCraftCore.modifyWorld) {
            EnumSpring.WATER.canGen = ConficGuiFactoryBC.get("worldgen.generateWaterSprings").getBoolean();
            springBlock = new BlockSpring();
            springBlock.setUnlocalizedName("eternalSpring");
            BCRegistry.INSTANCE.registerBlock(springBlock, ItemSpring.class, false);
        }

        woodenGearItem = (new ItemBuildCraft()).setTextureLocation("buildcraftcore:gear/wood").setUnlocalizedName("woodenGearItem");
        if (BCRegistry.INSTANCE.registerItem(woodenGearItem, false)) {
            OreDictionary.registerOre("gearWood", new ItemStack(woodenGearItem));
        }

        stoneGearItem = (new ItemBuildCraft()).setTextureLocation("buildcraftcore:gear/stone").setUnlocalizedName("stoneGearItem");
        if (BCRegistry.INSTANCE.registerItem(stoneGearItem, false)) {
            OreDictionary.registerOre("gearStone", new ItemStack(stoneGearItem));
        }

        ironGearItem = (new ItemBuildCraft()).setTextureLocation("buildcraftcore:gear/iron").setUnlocalizedName("ironGearItem");
        if (BCRegistry.INSTANCE.registerItem(ironGearItem, false)) {
            OreDictionary.registerOre("gearIron", new ItemStack(ironGearItem));
        }

        goldGearItem = (new ItemBuildCraft()).setTextureLocation("buildcraftcore:gear/gold").setUnlocalizedName("goldGearItem");
        if (BCRegistry.INSTANCE.registerItem(goldGearItem, false)) {
            OreDictionary.registerOre("gearGold", new ItemStack(goldGearItem));
        }

        diamondGearItem = (new ItemBuildCraft()).setTextureLocation("buildcraftcore:gear/diamond").setUnlocalizedName("diamondGearItem");
        if (BCRegistry.INSTANCE.registerItem(diamondGearItem, false)) {
            OreDictionary.registerOre("gearDiamond", new ItemStack(diamondGearItem));
        }

        paintbrushItem = new ItemPaintbrush();
        paintbrushItem.setUnlocalizedName("paintbrush");
        BCRegistry.INSTANCE.registerItem(paintbrushItem, false);

        if (DEVELOPER_MODE) {
            tabletItem = new ItemTablet();
            tabletItem.setUnlocalizedName("tablet");
            BCRegistry.INSTANCE.registerItem(tabletItem, false);
        }

        decoratedBlock = new BlockDecoration();
        decoratedBlock.setUnlocalizedName("decoratedBlock");
        BCRegistry.INSTANCE.registerBlock(decoratedBlock, true);

        engineBlock = (BlockEngine) CompatHooks.INSTANCE.getBlock(BlockEngine.class);
        BCRegistry.INSTANCE.registerBlock(engineBlock, ItemEngine.class, true);
        engineBlock.registerTile((Class<? extends TileEngineBase>) CompatHooks.INSTANCE.getTile(TileEngineWood.class), 0, "tile.engineWood");
        BCRegistry.INSTANCE.registerTileEntity(TileEngineWood.class, "buildcraft.core.engine.wood",
                "net.minecraft.src.buildcraft.energy.TileEngineWood");

        markerBlock = (BlockMarker) CompatHooks.INSTANCE.getBlock(BlockMarker.class);
        BCRegistry.INSTANCE.registerBlock(markerBlock.setUnlocalizedName("markerBlock"), false);

        pathMarkerBlock = (BlockPathMarker) CompatHooks.INSTANCE.getBlock(BlockPathMarker.class);
        BCRegistry.INSTANCE.registerBlock(pathMarkerBlock.setUnlocalizedName("pathMarkerBlock"), false);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new BlockHighlightHandler());
        MinecraftForge.EVENT_BUS.register(new ListTooltipHandler());
        MinecraftForge.EVENT_BUS.register(BucketHandler.INSTANCE);

        OreDictionary.registerOre("craftingTableWood", Blocks.crafting_table);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        BuildCraftAPI.fakePlayerProvider = CoreProxy.proxy;

        EntityRegistry.registerModEntity(EntityResizableCuboid.class, "bcResizCuboid", EntityIds.RESIZABLE_CUBOID, instance, 0, 100, false);

        ChannelHandler coreChannelHandler = new ChannelHandler();
        coreChannelHandler.registerPacketType(PacketTabletMessage.class);

        channels = NetworkRegistry.INSTANCE.newChannel(DefaultProps.NET_CHANNEL_NAME + "-CORE", coreChannelHandler, handler = new PacketHandler());

        achievementManager = new AchievementPageManager("BuildCraft");
        MinecraftForge.EVENT_BUS.register(achievementManager);

        woodenGearAchievement = achievementManager.registerAchievement(new Achievement("buildcraft|core:achievement.woodenGear",
                "woodenGearAchievement", 0, 0, woodenGearItem, null));
        stoneGearAchievement = achievementManager.registerAchievement(new Achievement("buildcraft|core:achievement.stoneGear", "stoneGearAchievement",
                2, 0, stoneGearItem, woodenGearAchievement));
        ironGearAchievement = achievementManager.registerAchievement(new Achievement("buildcraft|core:achievement.ironGear", "ironGearAchievement", 4,
                0, ironGearItem, stoneGearAchievement));
        goldGearAchievement = achievementManager.registerAchievement(new Achievement("buildcraft|core:achievement.goldGear", "goldGearAchievement", 6,
                0, goldGearItem, ironGearAchievement));
        diamondGearAchievement = achievementManager.registerAchievement(new Achievement("buildcraft|core:achievement.diamondGear",
                "diamondGearAchievement", 8, 0, diamondGearItem, goldGearAchievement));
        wrenchAchievement = achievementManager.registerAchievement(new Achievement("buildcraft|core:achievement.wrench", "wrenchAchievement", 3, 2,
                wrenchItem, stoneGearAchievement));
        engineRedstoneAchievement = BuildCraftCore.achievementManager.registerAchievement(new Achievement(
                "buildcraft|core:achievement.redstoneEngine", "engineAchievement1", 1, -2, new ItemStack(engineBlock, 1, 0),
                BuildCraftCore.woodenGearAchievement));

        StatementManager.registerParameterClass(StatementParameterItemStack.class);
        StatementManager.registerParameterClass(StatementParameterItemStackExact.class);
        StatementManager.registerParameterClass(StatementParameterDirection.class);
        StatementManager.registerParameterClass(StatementParamGateSideOnly.class);
        StatementManager.registerParameterClass(StatementParameterRedstoneLevel.class);
        StatementManager.registerTriggerProvider(new CoreTriggerProvider());
        StatementManager.registerActionProvider(new CoreActionProvider());

        if (BuildCraftCore.modifyWorld) {
            MinecraftForge.EVENT_BUS.register(new SpringPopulate());
        }

        if (mainConfiguration.hasChanged()) {
            mainConfiguration.save();
        }

        if (BuildCraftCore.loadDefaultRecipes) {
            loadRecipes();
        }

        BCCreativeTab.get("main").setIcon(new ItemStack(BuildCraftCore.wrenchItem, 1));

        EntityList.stringToClassMapping.remove("BuildCraft|Core.bcLaser");
        EntityList.stringToClassMapping.remove("BuildCraft|Core.bcEnergyLaser");

        BuilderAPI.schematicRegistry.registerSchematicBlock(engineBlock, SchematicEngine.class);

        CoreProxy.proxy.init();

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new CoreGuiHandler());

        MinecraftForge.EVENT_BUS.register(TabletManagerClient.INSTANCE);
        MinecraftForge.EVENT_BUS.register(TabletManagerServer.INSTANCE);
        MinecraftForge.EVENT_BUS.register(TickHandlerCore.INSTANCE);

        TabletAPI.registerProgram(new TabletProgramMenuFactory());

        // Create filler registry
        try {
            FillerManager.registry = new FillerRegistry();

            // INIT FILLER PATTERNS
            IFillerPattern[] patterns = { PatternFill.INSTANCE, PatternNone.INSTANCE, new PatternFlatten(), new PatternHorizon(), new PatternClear(),
                new PatternBox(), new PatternPyramid(), new PatternStairs(), new PatternCylinder(), new PatternFrame() };

            for (IFillerPattern pattern : patterns) {
                FillerManager.registry.addPattern(pattern);
            }
            ActionFiller.resetMap();

        } catch (Error error) {
            BCLog.logErrorAPI(error, IFillerPattern.class);
            throw error;
        }

        StatementManager.registerParameterClass(PatternParameterYDir.class);
        StatementManager.registerParameterClass(PatternParameterXZDir.class);
        StatementManager.registerParameterClass(PatternParameterCenter.class);
        StatementManager.registerParameterClass(PatternParameterHollow.class);

        ListRegistry.registerHandler(new ListMatchHandlerClass());
        ListRegistry.registerHandler(new ListMatchHandlerFluid());
        ListRegistry.registerHandler(new ListMatchHandlerTools());
        ListRegistry.registerHandler(new ListMatchHandlerArmor());
//        ListRegistry.registerHandler(new ListMatchHandler());
        ListRegistry.itemClassAsType.add(ItemFood.class);

        DebuggingTools.fmlInit();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        BCLog.logger.info("BuildCraft's fake player: UUID = " + gameProfile.getId().toString() + ", name = '" + gameProfile.getName() + "'!");

        BCRegistry.INSTANCE.save();

        for (Object o : Block.blockRegistry) {
            Block block = (Block) o;

            if (block instanceof BlockFluidBase || block instanceof BlockLiquid || block instanceof IPlantable) {
                BuildCraftAPI.softBlocks.add(block);
            }
        }

        BuildCraftAPI.softBlocks.add(Blocks.snow);
        BuildCraftAPI.softBlocks.add(Blocks.vine);
        BuildCraftAPI.softBlocks.add(Blocks.fire);

        CropManager.setDefaultHandler(new CropHandlerPlantable());
        CropManager.registerHandler(new CropHandlerReeds());

        BuildCraftAPI.registerWorldProperty("replaceable", new WorldPropertyIsReplaceable());
        BuildCraftAPI.registerWorldProperty("soft", new WorldPropertyIsSoft());
        BuildCraftAPI.registerWorldProperty("wood", new WorldPropertyIsWood());
        BuildCraftAPI.registerWorldProperty("leaves", new WorldPropertyIsLeaf());
        for (int i = 0; i < 4; i++) {
            BuildCraftAPI.registerWorldProperty("ore@hardness=" + i, new WorldPropertyIsOre(i));
        }
        BuildCraftAPI.registerWorldProperty("harvestable", new WorldPropertyIsHarvestable());
        BuildCraftAPI.registerWorldProperty("farmland", new WorldPropertyIsFarmland());
        BuildCraftAPI.registerWorldProperty("shoveled", new WorldPropertyIsShoveled());
        BuildCraftAPI.registerWorldProperty("dirt", new WorldPropertyIsDirt());
        BuildCraftAPI.registerWorldProperty("fluidSource", new WorldPropertyIsFluidSource());

        BuildCraftAPI.registerWorldProperty("freepath", new WorldPropertyIsFreePath());

        // Landmarks are often caught incorrectly, making building them counter-productive.
        SchematicRegistry.INSTANCE.registerSchematicBlock(markerBlock, SchematicIgnore.class);
        SchematicRegistry.INSTANCE.registerSchematicBlock(pathMarkerBlock, SchematicIgnore.class);

        ColorUtils.initialize();

        actionControl = new IActionExternal[IControllable.Mode.values().length];
        for (IControllable.Mode mode : IControllable.Mode.values()) {
            if (mode != IControllable.Mode.Unknown && mode != IControllable.Mode.Mode) {
                actionControl[mode.ordinal()] = new ActionMachineControl(mode);
            }
        }

        MinecraftForge.EVENT_BUS.register(ListOreDictionaryCache.INSTANCE);
        for (String s : OreDictionary.getOreNames()) {
            ListOreDictionaryCache.INSTANCE.registerName(s);
        }

        ListRegistry.registerHandler(new ListMatchHandlerOreDictionary());
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(commandBuildcraft);

        /* Increase the builder speed in singleplayer mode. Singleplayer users generally run far fewer of them. */

        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            BuildingSlotMapIterator.MAX_PER_ITEM = builderMaxPerItemFactor * 4;
        } else {
            BuildingSlotMapIterator.MAX_PER_ITEM = builderMaxPerItemFactor;
        }

        if (Utils.CAULDRON_DETECTED) {
            BCLog.logger.warn("############################################");
            BCLog.logger.warn("#                                          #");
            BCLog.logger.warn("#  Cauldron has been detected! Please keep #");
            BCLog.logger.warn("#  in mind that BuildCraft may NOT provide #");
            BCLog.logger.warn("# support to Cauldron users, as the mod is #");
            BCLog.logger.warn("# primarily tested without Bukkit/Spigot's #");
            BCLog.logger.warn("#    changes to the Minecraft internals.   #");
            BCLog.logger.warn("#                                          #");
            BCLog.logger.warn("#  Any lag caused by BuildCraft on top of  #");
            BCLog.logger.warn("#  Cauldron likely arises from workarounds #");
            BCLog.logger.warn("#  which we apply to make sure BuildCraft  #");
            BCLog.logger.warn("#  works properly with Cauldron installed. #");
            BCLog.logger.warn("#                                          #");
            BCLog.logger.warn("#     Thanks for your attention! ~ BC devs #");
            BCLog.logger.warn("#                                          #");
            BCLog.logger.warn("############################################");

            // To people reading that code and thinking we're lying:
            // Cauldron does not invalidate tile entities properly, causing
            // issues with our tile entity cache. That is the bug and that
            // is also the reason for the extra lag caused when using Cauldron.
        }
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        TabletManagerClient.INSTANCE.onServerStopping();
        TabletManagerServer.INSTANCE.onServerStopping();
    }

    public void reloadConfig(RestartRequirement restartType) {
        if (restartType == RestartRequirement.GAME) {
            modifyWorld = ConficGuiFactoryBC.get("worldgen.enable").getBoolean();
            updateFactor = ConficGuiFactoryBC.get("network.updateFactor").getInt();
            longUpdateFactor = ConficGuiFactoryBC.get("network.longUpdateFactor").getInt();
            colorBlindMode = ConficGuiFactoryBC.get("display.colorBlindMode").getBoolean();

            reloadConfig(RestartRequirement.WORLD);
        } else if (restartType == RestartRequirement.WORLD) {
            reloadConfig(RestartRequirement.NONE);
        } else {
            useServerDataOnClient = ConficGuiFactoryBC.get("general.useServerDataOnClient").getBoolean(true);
            builderMaxPerItemFactor = ConficGuiFactoryBC.get("general.builderMaxIterationsPerItemFactor").getInt();
            hideFluidNumbers = ConficGuiFactoryBC.get("display.hideFluidValues").getBoolean();
            hidePowerNumbers = ConficGuiFactoryBC.get("display.hidePowerValues").getBoolean();
            itemLifespan = ConficGuiFactoryBC.get("general.itemLifespan").getInt();
            canEnginesExplode = ConficGuiFactoryBC.get("general.canEnginesExplode").getBoolean();
            consumeWaterSources = ConficGuiFactoryBC.get("general.pumpsConsumeWater").getBoolean();
            miningMultiplier = (float) ConficGuiFactoryBC.get("power.miningUsageMultiplier").getDouble();
            miningAllowPlayerProtectedBlocks = ConficGuiFactoryBC.get("general.miningBreaksPlayerProtectedBlocks").getBoolean();

            BuildingSlotMapIterator.MAX_PER_ITEM = builderMaxPerItemFactor;

            ChannelHandler.setRecordStats(ConficGuiFactoryBC.get("debug.network.stats").getBoolean());

            if (mainConfiguration.hasChanged()) {
                mainConfiguration.save();
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if ("BuildCraft|Core".equals(event.modID)) {
            reloadConfig(event.isWorldRunning ? RestartRequirement.NONE : RestartRequirement.WORLD);
        }
    }

    public void loadRecipes() {
        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(wrenchItem), "I I", " G ", " I ", 'I', "ingotIron", 'G', "gearStone");
        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(woodenGearItem), " S ", "S S", " S ", 'S', "stickWood");
        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(stoneGearItem), " I ", "IGI", " I ", 'I', "cobblestone", 'G', "gearWood");
        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(ironGearItem), " I ", "IGI", " I ", 'I', "ingotIron", 'G', "gearStone");
        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(goldGearItem), " I ", "IGI", " I ", 'I', "ingotGold", 'G', "gearIron");
        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(diamondGearItem), " I ", "IGI", " I ", 'I', "gemDiamond", 'G', "gearGold");
        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(mapLocationItem), "ppp", "pYp", "ppp", 'p', Items.paper, 'Y', "dyeYellow");

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(engineBlock, 1, 0), "www", " g ", "GpG", 'w', "plankWood", 'g', "blockGlass", 'G',
                "gearWood", 'p', Blocks.piston);

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(markerBlock, 1), "l ", "r ", 'l', new ItemStack(Items.dye, 1, 4), 'r',
                Blocks.redstone_torch);

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(pathMarkerBlock, 1), "l ", "r ", 'l', "dyeGreen", 'r', Blocks.redstone_torch);

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(paintbrushItem), " iw", " gi", "s  ", 's', "stickWood", 'g', "gearWood", 'w',
                new ItemStack(Blocks.wool, 1, 0), 'i', Items.string);

        ItemStack anyPaintbrush = new ItemStack(paintbrushItem, 1, OreDictionary.WILDCARD_VALUE);

        for (int i = 0; i < 16; i++) {
            ItemStack outputStack = paintbrushItem.getItemStack(EnumDyeColor.byMetadata(i));
            BCRegistry.INSTANCE.addShapelessRecipe(outputStack, anyPaintbrush, EnumColor.fromId(i).getDye());
        }

        // Convert old lists to new lists
        BCRegistry.INSTANCE.addShapelessRecipe(new ItemStack(listItem, 1, 1), new ItemStack(listItem, 1, 0));

        if (Loader.isModLoaded("BuildCraft|Silicon")) {
            CoreSiliconRecipes.loadSiliconRecipes();
        } else {
            BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(listItem, 1, 1), "ppp", "pYp", "ppp", 'p', Items.paper, 'Y', "dyeGreen");
        }
    }

    @Mod.EventHandler
    public void processIMCRequests(FMLInterModComms.IMCEvent event) {
        InterModComms.processIMC(event);
    }

    @SubscribeEvent
    public void cleanRegistries(WorldEvent.Unload event) {
        for (IWorldProperty property : BuildCraftAPI.worldProperties.values()) {
            property.clear();
        }
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            TilePathMarker.clearAvailableMarkersList(event.world);
        }
    }

    // 1.7.10 migration
    @Mod.EventHandler
    public void remap(FMLMissingMappingsEvent event) {
        BCLog.logger.info("Core|Remap " + System.identityHashCode(event));
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.getAll()) {
            String name = mapping.name;
            BCLog.logger.info("        - " + name);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void loadTextures(TextureStitchEvent.Pre evt) {
        StatementManager.registerIcons(evt.map);
        CoreIconProvider.registerSprites(evt.map);
        TextureAtlasSprite[] array = new TextureAtlasSprite[16];
        for (EnumColor color : EnumColor.values()) {
            String location = "buildcraftcore:items/paintbrush/" + color.name().toLowerCase(Locale.ROOT);
            array[color.ordinal()] = evt.map.registerSprite(new ResourceLocation(location));
        }

        EnumColor.registerSprites(array);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void renderOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!mc.gameSettings.showDebugInfo) return;
        if (mc.thePlayer.hasReducedDebug() || mc.gameSettings.reducedDebugInfo || !mc.thePlayer.capabilities.isCreativeMode) {
            return;
        }
        event.right.add("");
        event.right.add("BC PQS|" + PacketHandler.packetQueueSize());

        MovingObjectPosition object = mc.objectMouseOver;
        if (object == null) {
            return;
        }
        MovingObjectType type = object.typeOfHit;

        if (type == MovingObjectType.BLOCK && object.getBlockPos() != null) {
            BlockPos pos = object.getBlockPos();
            TileEntity tile = mc.theWorld.getTileEntity(pos);

            if (tile instanceof IDebuggable) {
                ((IDebuggable) tile).getDebugInfo(event.left, event.right, object.sideHit);
            }
        } else if (type == MovingObjectType.ENTITY) {
            Entity ent = object.entityHit;
            if (ent instanceof IDebuggable) {
                ((IDebuggable) ent).getDebugInfo(event.left, event.right, object.sideHit);
            }
        }
    }

    private Set<Integer> worldsNeedingUpdate = new HashSet<>();

    @SubscribeEvent
    public void worldTickEvent(WorldTickEvent event) {
        if (event.side == Side.CLIENT) return;
        int dimId = event.world.provider.getDimension();
        if (worldsNeedingUpdate.contains(dimId)) {
            worldsNeedingUpdate.remove(dimId);
            for (Object obj : event.world.loadedTileEntityList) {
                TileEntity tile = (TileEntity) obj;
                if (tile instanceof IAdditionalDataTile) {
                    ((IAdditionalDataTile) tile).sendNetworkUpdate();
                }
            }
        }
    }

    @SubscribeEvent
    public void playerJoinWorld(EntityJoinWorldEvent event) {
        worldsNeedingUpdate.add(event.world.provider.getDimension());
    }
}

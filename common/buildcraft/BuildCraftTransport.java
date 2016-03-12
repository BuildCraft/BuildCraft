/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumColor;
import buildcraft.api.facades.FacadeAPI;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.lists.ListRegistry;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.transport.ICustomPipeConnection;
import buildcraft.api.transport.PipeConnectionAPI;
import buildcraft.api.transport.PipeManager;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.*;
import buildcraft.core.config.ConfigManager;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.network.base.ChannelHandler;
import buildcraft.core.lib.network.base.PacketHandler;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.ModelHelper;
import buildcraft.transport.*;
import buildcraft.transport.block.BlockPipe;
import buildcraft.transport.gates.*;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.network.*;
import buildcraft.transport.pipes.*;
import buildcraft.transport.pluggable.*;
import buildcraft.transport.render.FacadeItemModel;
import buildcraft.transport.render.GateItemModel;
import buildcraft.transport.render.PipeBlockModel;
import buildcraft.transport.render.PipeItemModel;
import buildcraft.transport.render.tile.PipeRendererFluids;
import buildcraft.transport.schematics.BptPipeFiltered;
import buildcraft.transport.schematics.BptPipeRotatable;
import buildcraft.transport.schematics.SchematicPipe;
import buildcraft.transport.statements.*;
import buildcraft.transport.statements.ActionValve.ValveState;
import buildcraft.transport.statements.TriggerClockTimer.Time;
import buildcraft.transport.statements.TriggerPipeContents.PipeContents;
import buildcraft.transport.stripes.*;

@Mod(version = DefaultProps.VERSION, modid = "BuildCraft|Transport", name = "Buildcraft Transport", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftTransport extends BuildCraftMod {
    /** Neptune pipes! */
    // Test against this being a dev environment by checking if the version is a real one or not. (It is automatically
    // changed from "@VERSION@" to a real version string at build time)
    public static final boolean NEPTUNE_TESTING = DefaultProps.VERSION.contains("@");

    @Mod.Instance("BuildCraft|Transport")
    public static BuildCraftTransport instance;

    public static float pipeDurability;
    public static int pipeFluidsBaseFlowRate;
    public static boolean facadeTreatBlacklistAsWhitelist;
    public static boolean additionalWaterproofingRecipe;
    public static boolean facadeForceNonLaserRecipe;
    public static boolean showAllFacadesCreative;

    public static BlockGenericPipe genericPipeBlock;
    public static BlockFilteredBuffer filteredBufferBlock;
    public static BlockPipe pipeBlock;

    public static Item pipeWaterproof;
    public static ItemGate pipeGate;
    public static Item pipeWire;
    public static Item plugItem;
    public static ItemLens lensItem;
    public static Item powerAdapterItem;
    public static Item pipeStructureCobblestone;
    public static Item gateCopier;
    public static ItemFacade facadeItem;

    public static Item pipeItemsWood;
    public static Item pipeItemsEmerald;
    public static Item pipeItemsStone;
    public static Item pipeItemsCobblestone;
    public static Item pipeItemsIron;
    public static Item pipeItemsQuartz;
    public static Item pipeItemsGold;
    public static Item pipeItemsDiamond;
    public static Item pipeItemsObsidian;
    public static Item pipeItemsLapis;
    public static Item pipeItemsDaizuli;
    public static Item pipeItemsVoid;
    public static Item pipeItemsSandstone;
    public static Item pipeItemsEmzuli;
    public static Item pipeItemsStripes;
    public static Item pipeItemsClay;
    public static Item pipeFluidsWood;
    public static Item pipeFluidsCobblestone;
    public static Item pipeFluidsStone;
    public static Item pipeFluidsQuartz;
    public static Item pipeFluidsIron;
    public static Item pipeFluidsGold;
    public static Item pipeFluidsVoid;
    public static Item pipeFluidsSandstone;
    public static Item pipeFluidsEmerald;
    public static Item pipeFluidsDiamond;
    public static Item pipeFluidsClay;
    public static Item pipePowerWood;
    public static Item pipePowerCobblestone;
    public static Item pipePowerStone;
    public static Item pipePowerQuartz;
    public static Item pipePowerIron;
    public static Item pipePowerGold;
    public static Item pipePowerDiamond;
    public static Item pipePowerEmerald;
    public static Item pipePowerSandstone;

    public static String[] facadeBlacklist;

    public static ITriggerInternal triggerLightSensorBright, triggerLightSensorDark;
    public static ITriggerInternal[] triggerPipe = new ITriggerInternal[PipeContents.values().length];
    public static ITriggerInternal[] triggerPipeWireActive = new ITriggerInternal[PipeWire.values().length];
    public static ITriggerInternal[] triggerPipeWireInactive = new ITriggerInternal[PipeWire.values().length];
    public static ITriggerInternal[] triggerTimer = new ITriggerInternal[TriggerClockTimer.Time.VALUES.length];
    public static ITriggerInternal[] triggerRedstoneLevel = new ITriggerInternal[15];
    public static IActionInternal[] actionPipeWire = new ActionSignalOutput[PipeWire.values().length];
    public static IActionInternal actionEnergyPulser = new ActionEnergyPulsar();
    public static IActionInternal actionSingleEnergyPulse = new ActionSingleEnergyPulse();
    public static IActionInternal[] actionPipeColor = new IActionInternal[16];
    public static IActionInternal[] actionPipeDirection = new IActionInternal[16];
    public static IActionInternal[] actionPowerLimiter = new IActionInternal[7];
    public static IActionInternal[] actionRedstoneLevel = new IActionInternal[15];
    public static IActionInternal actionExtractionPresetRed = new ActionExtractionPreset(EnumColor.RED);
    public static IActionInternal actionExtractionPresetBlue = new ActionExtractionPreset(EnumColor.BLUE);
    public static IActionInternal actionExtractionPresetGreen = new ActionExtractionPreset(EnumColor.GREEN);
    public static IActionInternal actionExtractionPresetYellow = new ActionExtractionPreset(EnumColor.YELLOW);
    public static IActionInternal[] actionValve = new IActionInternal[4];

    public static boolean debugPrintFacadeList = false;
    public static boolean usePipeLoss = false;

    public static float gateCostMultiplier = 1.0F;

    public static PipeExtensionListener pipeExtensionListener;

    private static LinkedList<PipeRecipe> pipeRecipes = new LinkedList<PipeRecipe>();
    private static ChannelHandler transportChannelHandler;

    public PipeIconProvider pipeIconProvider = new PipeIconProvider();
    public WireIconProvider wireIconProvider = new WireIconProvider();

    private static class PipeRecipe {
        boolean isShapeless = false; // pipe recipes come shaped and unshaped.
        ItemStack result;
        Object[] input;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        // TODO Fluid shader rendering
        // FluidShaderManager.INSTANCE.getRenderer(null);
        new BCCreativeTab("pipes");
        if (NEPTUNE_TESTING) new BCCreativeTab("neptune");
        if (Loader.isModLoaded("BuildCraft|Silicon")) {
            new BCCreativeTab("gates");
        }

        try {
            BuildCraftCore.mainConfigManager.register("experimental.kinesisPowerLossOnTravel", false,
                    "Should kinesis pipes lose power over distance (think IC2 or BC pre-3.7)?", ConfigManager.RestartRequirement.WORLD);

            BuildCraftCore.mainConfigManager.register("general.pipes.hardness", DefaultProps.PIPES_DURABILITY, "How hard to break should a pipe be?",
                    ConfigManager.RestartRequirement.NONE);
            BuildCraftCore.mainConfigManager.register("general.pipes.baseFluidRate", DefaultProps.PIPES_FLUIDS_BASE_FLOW_RATE,
                    "What should the base flow rate of a fluid pipe be?", ConfigManager.RestartRequirement.GAME).setMinValue(1).setMaxValue(40);
            BuildCraftCore.mainConfigManager.register("debug.printFacadeList", false, "Print a list of all registered facades.",
                    ConfigManager.RestartRequirement.GAME);
            BuildCraftCore.mainConfigManager.register("general.pipes.facadeShowAllInCreative", true,
                    "Should all BC facades be shown in Creative/NEI, or just a few carefully chosen ones?", ConfigManager.RestartRequirement.GAME);
            BuildCraftCore.mainConfigManager.register("general.pipes.slimeballWaterproofRecipe", false,
                    "Should I enable an alternate Waterproof recipe, based on slimeballs?", ConfigManager.RestartRequirement.GAME);
            BuildCraftCore.mainConfigManager.register("power.gateCostMultiplier", 1.0D, "What should be the multiplier of all gate power costs?",
                    ConfigManager.RestartRequirement.GAME);
            BuildCraftCore.mainConfigManager.register("general.pipes.facadeBlacklist", new String[] {
                //@formatter:off
					Block.blockRegistry.getNameForObject(Blocks.end_portal_frame).toString(),
					Block.blockRegistry.getNameForObject(Blocks.grass).toString(),
					Block.blockRegistry.getNameForObject(Blocks.leaves).toString(),
					Block.blockRegistry.getNameForObject(Blocks.leaves2).toString(),
					Block.blockRegistry.getNameForObject(Blocks.lit_pumpkin).toString(),
					Block.blockRegistry.getNameForObject(Blocks.lit_redstone_lamp).toString(),
					Block.blockRegistry.getNameForObject(Blocks.mob_spawner).toString(),
					Block.blockRegistry.getNameForObject(Blocks.monster_egg).toString(),
					Block.blockRegistry.getNameForObject(Blocks.redstone_lamp).toString(),
					Block.blockRegistry.getNameForObject(Blocks.double_stone_slab).toString(),
					Block.blockRegistry.getNameForObject(Blocks.double_wooden_slab).toString(),
					Block.blockRegistry.getNameForObject(Blocks.sponge).toString()
					//@formatter:on
            }, "What block types should be blacklisted from being a facade?", ConfigManager.RestartRequirement.GAME);
            BuildCraftCore.mainConfigManager.register("general.pipes.facadeBlacklistAsWhitelist", false,
                    "Should the blacklist be treated as a whitelist instead?", ConfigManager.RestartRequirement.GAME);
            BuildCraftCore.mainConfigManager.register("general.pipes.facadeNoLaserRecipe", false,
                    "Should non-laser (crafting table) facade recipes be forced?", ConfigManager.RestartRequirement.GAME);

            reloadConfig(ConfigManager.RestartRequirement.GAME);

            if (showAllFacadesCreative) {
                new BCCreativeTab("facades");
            }

            filteredBufferBlock = new BlockFilteredBuffer();
            BCRegistry.INSTANCE.registerBlock(filteredBufferBlock.setUnlocalizedName("filteredBufferBlock"), false);

            pipeWaterproof = new ItemBuildCraft();
            pipeWaterproof.setUnlocalizedName("pipeWaterproof");
            BCRegistry.INSTANCE.registerItem(pipeWaterproof, false);

            genericPipeBlock = (BlockGenericPipe) CompatHooks.INSTANCE.getBlock(BlockGenericPipe.class);
            BCRegistry.INSTANCE.registerBlock(genericPipeBlock.setUnlocalizedName("pipeBlock"), ItemBlock.class, true);

            pipeItemsWood = buildPipe(PipeItemsWood.class, "plankWood", "blockGlassColorless", "plankWood");
            pipeItemsEmerald = buildPipe(PipeItemsEmerald.class, "gemEmerald", "blockGlassColorless", "gemEmerald");
            pipeItemsCobblestone = buildPipe(PipeItemsCobblestone.class, "cobblestone", "blockGlassColorless", "cobblestone");
            pipeItemsStone = buildPipe(PipeItemsStone.class, "stone", "blockGlassColorless", "stone");
            pipeItemsQuartz = buildPipe(PipeItemsQuartz.class, "blockQuartz", "blockGlassColorless", "blockQuartz");
            pipeItemsIron = buildPipe(PipeItemsIron.class, "ingotIron", "blockGlassColorless", "ingotIron");
            pipeItemsGold = buildPipe(PipeItemsGold.class, "ingotGold", "blockGlassColorless", "ingotGold");
            pipeItemsDiamond = buildPipe(PipeItemsDiamond.class, "gemDiamond", "blockGlassColorless", "gemDiamond");
            pipeItemsObsidian = buildPipe(PipeItemsObsidian.class, Blocks.obsidian, "blockGlassColorless", Blocks.obsidian);
            pipeItemsLapis = buildPipe(PipeItemsLapis.class, "blockLapis", "blockGlassColorless", "blockLapis");
            pipeItemsDaizuli = buildPipe(PipeItemsDaizuli.class, "blockLapis", "blockGlassColorless", "gemDiamond");
            pipeItemsSandstone = buildPipe(PipeItemsSandstone.class, Blocks.sandstone, "blockGlassColorless", Blocks.sandstone);
            pipeItemsVoid = buildPipe(PipeItemsVoid.class, "dyeBlack", "blockGlassColorless", "dustRedstone");
            pipeItemsEmzuli = buildPipe(PipeItemsEmzuli.class, "blockLapis", "blockGlassColorless", "gemEmerald");
            pipeItemsStripes = buildPipe(PipeItemsStripes.class, "gearGold", "blockGlassColorless", "gearGold");
            pipeItemsClay = buildPipe(PipeItemsClay.class, Blocks.clay, "blockGlassColorless", Blocks.clay);

            pipeFluidsWood = buildPipe(PipeFluidsWood.class, pipeWaterproof, pipeItemsWood);
            pipeFluidsCobblestone = buildPipe(PipeFluidsCobblestone.class, pipeWaterproof, pipeItemsCobblestone);
            pipeFluidsStone = buildPipe(PipeFluidsStone.class, pipeWaterproof, pipeItemsStone);
            pipeFluidsQuartz = buildPipe(PipeFluidsQuartz.class, pipeWaterproof, pipeItemsQuartz);
            pipeFluidsIron = buildPipe(PipeFluidsIron.class, pipeWaterproof, pipeItemsIron);
            pipeFluidsGold = buildPipe(PipeFluidsGold.class, pipeWaterproof, pipeItemsGold);
            pipeFluidsEmerald = buildPipe(PipeFluidsEmerald.class, pipeWaterproof, pipeItemsEmerald);
            pipeFluidsDiamond = buildPipe(PipeFluidsDiamond.class, pipeWaterproof, pipeItemsDiamond);
            pipeFluidsSandstone = buildPipe(PipeFluidsSandstone.class, pipeWaterproof, pipeItemsSandstone);
            pipeFluidsVoid = buildPipe(PipeFluidsVoid.class, pipeWaterproof, pipeItemsVoid);
            pipeFluidsClay = buildPipe(PipeFluidsClay.class, pipeWaterproof, pipeItemsClay);

            pipePowerWood = buildPipe(PipePowerWood.class, "dustRedstone", pipeItemsWood);
            pipePowerCobblestone = buildPipe(PipePowerCobblestone.class, "dustRedstone", pipeItemsCobblestone);
            pipePowerStone = buildPipe(PipePowerStone.class, "dustRedstone", pipeItemsStone);
            pipePowerQuartz = buildPipe(PipePowerQuartz.class, "dustRedstone", pipeItemsQuartz);
            pipePowerIron = buildPipe(PipePowerIron.class, "dustRedstone", pipeItemsIron);
            pipePowerGold = buildPipe(PipePowerGold.class, "dustRedstone", pipeItemsGold);
            pipePowerDiamond = buildPipe(PipePowerDiamond.class, "dustRedstone", pipeItemsDiamond);
            pipePowerEmerald = buildPipe(PipePowerEmerald.class, "dustRedstone", pipeItemsEmerald);
            pipePowerSandstone = buildPipe(PipePowerSandstone.class, "dustRedstone", pipeItemsSandstone);

            pipeStructureCobblestone = buildPipe(PipeStructureCobblestone.class, Blocks.cobblestone, Blocks.gravel, Blocks.cobblestone);

            pipeWire = new ItemPipeWire();
            BCRegistry.INSTANCE.registerItem(pipeWire, false);
            PipeWire.item = pipeWire;

            pipeGate = new ItemGate();
            pipeGate.setUnlocalizedName("pipeGate");
            BCRegistry.INSTANCE.registerItem(pipeGate, false);

            facadeItem = new ItemFacade();
            facadeItem.setUnlocalizedName("pipeFacade");
            BCRegistry.INSTANCE.registerItem(facadeItem, false);
            FacadeAPI.facadeItem = facadeItem;

            plugItem = new ItemPlug();
            plugItem.setUnlocalizedName("pipePlug");
            BCRegistry.INSTANCE.registerItem(plugItem, false);

            lensItem = new ItemLens();
            lensItem.setUnlocalizedName("pipeLens");
            BCRegistry.INSTANCE.registerItem(lensItem, false);

            powerAdapterItem = new ItemPowerAdapter();
            powerAdapterItem.setUnlocalizedName("pipePowerAdapter");
            BCRegistry.INSTANCE.registerItem(powerAdapterItem, false);

            gateCopier = new ItemGateCopier();
            BCRegistry.INSTANCE.registerItem(gateCopier, false);

            for (PipeContents kind : PipeContents.values()) {
                triggerPipe[kind.ordinal()] = new TriggerPipeContents(kind);
            }

            for (PipeWire wire : PipeWire.values()) {
                triggerPipeWireActive[wire.ordinal()] = new TriggerPipeSignal(true, wire);
                triggerPipeWireInactive[wire.ordinal()] = new TriggerPipeSignal(false, wire);
                actionPipeWire[wire.ordinal()] = new ActionSignalOutput(wire);
            }

            for (Time time : TriggerClockTimer.Time.VALUES) {
                triggerTimer[time.ordinal()] = new TriggerClockTimer(time);
            }

            for (int level = 0; level < triggerRedstoneLevel.length; level++) {
                triggerRedstoneLevel[level] = new TriggerRedstoneFaderInput(level + 1);
                actionRedstoneLevel[level] = new ActionRedstoneFaderOutput(level + 1);
            }

            for (EnumDyeColor color : EnumDyeColor.values()) {
                actionPipeColor[color.ordinal()] = new ActionPipeColor(color);
            }

            for (EnumFacing direction : EnumFacing.VALUES) {
                actionPipeDirection[direction.ordinal()] = new ActionPipeDirection(direction);
            }

            for (ValveState state : ValveState.VALUES) {
                actionValve[state.ordinal()] = new ActionValve(state);
            }

            for (PowerMode limit : PowerMode.VALUES) {
                actionPowerLimiter[limit.ordinal()] = new ActionPowerLimiter(limit);
            }

            triggerLightSensorBright = new TriggerLightSensor(true);
            triggerLightSensorDark = new TriggerLightSensor(false);
        } finally {
            BuildCraftCore.mainConfiguration.save();
        }

        InterModComms.registerHandler(new IMCHandlerTransport());
        if (NEPTUNE_TESTING) {
            pipeBlock = new BlockPipe();
            /* Ideally we would use "pipeBlock" but its taken by the generic pipe. Using pipeBlockNeptune will probably
             * be fine for us though */
            pipeBlock.setUnlocalizedName("pipeBlockNeptune");
            BCRegistry.INSTANCE.registerBlock(pipeBlock,
                    /* Register the block without registering an item - each pipe creates an item for itself. */
                    null,
                    /* Force register the pipe block- its probably a mistake for it to be disabled in the config, as you
                     * can already disable pipes indervidually. We don't mind too much about using an additional block
                     * id. */
                    true);

            // Let a seperate class handle all of the pipe initialisation- there is a large amount of it to do and we
            // don't need to make this even bigger than what it already is
            TransportPipes_BC8.preInit();
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        transportChannelHandler = new ChannelHandler();
        MinecraftForge.EVENT_BUS.register(this);

        transportChannelHandler.registerPacketType(PacketFluidUpdate.class);
        transportChannelHandler.registerPacketType(PacketPipeTransportItemStack.class);
        transportChannelHandler.registerPacketType(PacketPipeTransportItemStackRequest.class);
        transportChannelHandler.registerPacketType(PacketPipeTransportTraveler.class);
        transportChannelHandler.registerPacketType(PacketPowerUpdate.class);

        channels = NetworkRegistry.INSTANCE.newChannel(DefaultProps.NET_CHANNEL_NAME + "-TRANSPORT", transportChannelHandler, new PacketHandler());

        TransportProxy.proxy.registerTileEntities();

        BuilderAPI.schematicRegistry.registerSchematicBlock(genericPipeBlock, SchematicPipe.class);
        BuilderAPI.schematicRegistry.registerSchematicBlock(filteredBufferBlock, SchematicTile.class);

        new BptPipeRotatable(pipeItemsWood);
        new BptPipeRotatable(pipeFluidsWood);
        new BptPipeRotatable(pipeItemsIron);
        new BptPipeRotatable(pipeFluidsIron);
        new BptPipeRotatable(pipeItemsEmerald);
        new BptPipeRotatable(pipeFluidsEmerald);

        new BptPipeRotatable(pipeItemsDaizuli);
        new BptPipeRotatable(pipeItemsEmzuli);

        for (Item itemPipe : BlockGenericPipe.pipes.keySet()) {
            Class<? extends Pipe<?>> klazz = BlockGenericPipe.pipes.get(itemPipe);

            if (IDiamondPipe.class.isAssignableFrom(klazz)) {
                new BptPipeFiltered(itemPipe);
            }
        }

        PipeEventBus.registerGlobalHandler(new LensFilterHandler());

        BCCreativeTab.get("pipes").setIcon(new ItemStack(BuildCraftTransport.pipeItemsDiamond, 1));
        if (NEPTUNE_TESTING) BCCreativeTab.get("neptune").setIcon(new ItemStack(Items.cake));
        if (showAllFacadesCreative) {
            BCCreativeTab.get("facades").setIcon(facadeItem.getFacadeForBlock(Blocks.brick_block.getDefaultState()));
        }
        if (Loader.isModLoaded("BuildCraft|Silicon")) {
            BCCreativeTab.get("gates").setIcon(ItemGate.makeGateItem(GateMaterial.DIAMOND, GateLogic.AND));
        }

        StatementManager.registerParameterClass(TriggerParameterSignal.class);
        StatementManager.registerParameterClass(ActionParameterSignal.class);
        StatementManager.registerTriggerProvider(new PipeTriggerProvider());
        StatementManager.registerActionProvider(new PipeActionProvider());

        // Item use stripes handlers
        PipeManager.registerStripesHandler(new StripesHandlerRightClick(), -32768);
        PipeManager.registerStripesHandler(new StripesHandlerDispenser(), -49152);
        PipeManager.registerStripesHandler(new StripesHandlerPlant(), 0);
        PipeManager.registerStripesHandler(new StripesHandlerBucket(), 0);
        PipeManager.registerStripesHandler(new StripesHandlerArrow(), 0);
        PipeManager.registerStripesHandler(new StripesHandlerShears(), 0);
        PipeManager.registerStripesHandler(new StripesHandlerPipes(), 0);
        PipeManager.registerStripesHandler(new StripesHandlerPipeWires(), 0);
        PipeManager.registerStripesHandler(new StripesHandlerEntityInteract(), 0);
        PipeManager.registerStripesHandler(new StripesHandlerPlaceBlock(), -65536);
        PipeManager.registerStripesHandler(new StripesHandlerUse(), -131072);
        PipeManager.registerStripesHandler(new StripesHandlerHoe(), 0);

        StripesHandlerDispenser.items.add(ItemMinecart.class);
        StripesHandlerRightClick.items.add(Items.egg);
        StripesHandlerRightClick.items.add(Items.snowball);
        StripesHandlerRightClick.items.add(Items.experience_bottle);
        StripesHandlerUse.items.add(Items.fireworks);

        // Block breaking stripes handlers
        PipeManager.registerStripesHandler(new StripesHandlerMinecartDestroy(), 0);

        PipeManager.registerPipePluggable(FacadePluggable.class, "facade");
        PipeManager.registerPipePluggable(GatePluggable.class, "gate");
        PipeManager.registerPipePluggable(LensPluggable.class, "lens");
        PipeManager.registerPipePluggable(PlugPluggable.class, "plug");
        PipeManager.registerPipePluggable(PowerAdapterPluggable.class, "powerAdapter");

        GateExpansions.registerExpansion(GateExpansionPulsar.INSTANCE);
        GateExpansions.registerExpansion(GateExpansionTimer.INSTANCE);
        GateExpansions.registerExpansion(GateExpansionRedstoneFader.INSTANCE);
        GateExpansions.registerExpansion(GateExpansionLightSensor.INSTANCE, new ItemStack(Blocks.daylight_detector));

        if (BuildCraftCore.loadDefaultRecipes) {
            loadRecipes();
        }

        TransportProxy.proxy.registerRenderers();
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new TransportGuiHandler());

        /* Make pipes extend to connect to blocks like chests. This means that a connection going UP (the bottom of the
         * block in question) will be the only face that does not extend into the block slightly. */
        ICustomPipeConnection smallerBlockConnection = (world, pos, face, state) -> face == EnumFacing.UP ? 0 : 2 / 16f;

        PipeConnectionAPI.registerConnection(Blocks.chest, smallerBlockConnection);
        PipeConnectionAPI.registerConnection(Blocks.trapped_chest, smallerBlockConnection);
        PipeConnectionAPI.registerConnection(Blocks.hopper, smallerBlockConnection);

        if (NEPTUNE_TESTING) TransportPipes_BC8.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        facadeItem.initialize();

        if (debugPrintFacadeList) {
            try {
                PrintWriter writer = new PrintWriter("FacadeDebug.txt", "UTF-8");
                writer.println("*** REGISTERED FACADES ***");
                for (ItemStack stack : ItemFacade.allFacades) {
                    if (facadeItem.getBlockStatesForFacade(stack).length > 0) {
                        writer.println(facadeItem.getBlockStatesForFacade(stack)[0]);
                    }
                }
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (NEPTUNE_TESTING) TransportPipes_BC8.postInit();
        ListRegistry.itemClassAsType.add(ItemPipe.class);
        ListRegistry.itemClassAsType.add(ItemGate.class);
        ListRegistry.itemClassAsType.add(ItemFacade.class);
        ListRegistry.itemClassAsType.add(ItemPipeWire.class);
    }

    public void reloadConfig(ConfigManager.RestartRequirement restartType) {
        if (restartType == ConfigManager.RestartRequirement.GAME) {
            facadeTreatBlacklistAsWhitelist = BuildCraftCore.mainConfigManager.get("general.pipes.facadeBlacklistAsWhitelist").getBoolean();
            facadeBlacklist = BuildCraftCore.mainConfigManager.get("general.pipes.facadeBlacklist").getStringList();
            gateCostMultiplier = (float) BuildCraftCore.mainConfigManager.get("power.gateCostMultiplier").getDouble();
            additionalWaterproofingRecipe = BuildCraftCore.mainConfigManager.get("general.pipes.slimeballWaterproofRecipe").getBoolean();
            debugPrintFacadeList = BuildCraftCore.mainConfigManager.get("debug.printFacadeList").getBoolean();
            pipeFluidsBaseFlowRate = BuildCraftCore.mainConfigManager.get("general.pipes.baseFluidRate").getInt();
            facadeForceNonLaserRecipe = BuildCraftCore.mainConfigManager.get("general.pipes.facadeNoLaserRecipe").getBoolean();
            showAllFacadesCreative = BuildCraftCore.mainConfigManager.get("general.pipes.facadeShowAllInCreative").getBoolean();

            reloadConfig(ConfigManager.RestartRequirement.WORLD);
        } else if (restartType == ConfigManager.RestartRequirement.WORLD) {
            usePipeLoss = BuildCraftCore.mainConfigManager.get("experimental.kinesisPowerLossOnTravel").getBoolean();

            reloadConfig(ConfigManager.RestartRequirement.NONE);
        } else {
            pipeDurability = (float) BuildCraftCore.mainConfigManager.get("general.pipes.hardness").getDouble();

            if (BuildCraftCore.mainConfiguration.hasChanged()) {
                BuildCraftCore.mainConfiguration.save();
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if ("BuildCraft|Core".equals(event.modID)) {
            reloadConfig(event.isWorldRunning ? ConfigManager.RestartRequirement.NONE : ConfigManager.RestartRequirement.WORLD);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void textureHook(TextureStitchEvent.Pre event) {
        for (ItemPipe i : BlockGenericPipe.pipes.keySet()) {
            Pipe<?> dummyPipe = BlockGenericPipe.createPipe(i);
            if (dummyPipe != null) {
                dummyPipe.getIconProvider().registerIcons(event.map);
            }
        }

        WireIconProvider.registerIcons(event.map);

        for (GateDefinition.GateMaterial material : GateDefinition.GateMaterial.VALUES) {
            material.registerBlockIcon(event.map);
        }

        for (GateDefinition.GateLogic logic : GateDefinition.GateLogic.VALUES) {
            logic.registerBlockIcon(event.map);
        }

        for (IGateExpansion expansion : GateExpansions.getExpansions()) {
            expansion.textureStitch(event.map);
        }

        TriggerParameterSignal.registerIcons(event);
        ActionParameterSignal.registerIcons(event);
    }

    @Mod.EventHandler
    public void serverLoading(FMLServerStartingEvent event) {
        pipeExtensionListener = new PipeExtensionListener();
        MinecraftForge.EVENT_BUS.register(pipeExtensionListener);
    }

    @Mod.EventHandler
    public void serverUnloading(FMLServerStoppingEvent event) {
        // One last tick. This saves us from having to read/write this from/to disk
        for (WorldServer w : DimensionManager.getWorlds()) {
            pipeExtensionListener.tick(new TickEvent.WorldTickEvent(Side.SERVER, TickEvent.Phase.END, w));
        }
        MinecraftForge.EVENT_BUS.unregister(pipeExtensionListener);
        pipeExtensionListener = null;
    }

    public static void loadRecipes() {
        // Add base recipe for pipe waterproof.
        GameRegistry.addShapelessRecipe(new ItemStack(pipeWaterproof, 1), new ItemStack(Items.dye, 1, 2));
        if (additionalWaterproofingRecipe) {
            GameRegistry.addShapelessRecipe(new ItemStack(pipeWaterproof, 1), new ItemStack(Items.slime_ball));
        }

        // Add pipe recipes
        for (PipeRecipe pipe : pipeRecipes) {
            if (pipe.isShapeless) {
                BCRegistry.INSTANCE.addShapelessRecipe(pipe.result, pipe.input);
            } else {
                BCRegistry.INSTANCE.addCraftingRecipe(pipe.result, pipe.input);
            }
        }

        GameRegistry.addRecipe(new PipeColoringRecipe());
        RecipeSorter.register("buildcraft:pipecoloring", PipeColoringRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(filteredBufferBlock, 1), "wdw", "wcw", "wpw", 'w', "plankWood", 'd',
                BuildCraftTransport.pipeItemsDiamond, 'c', "chestWood", 'p', Blocks.piston);

        // Facade turning helper
        GameRegistry.addRecipe(facadeItem.new FacadeRecipe());
        RecipeSorter.register("facadeTurningHelper", ItemFacade.FacadeRecipe.class, RecipeSorter.Category.SHAPELESS, "");

        // Pipe Plug
        GameRegistry.addShapelessRecipe(new ItemStack(plugItem, 4), new ItemStack(pipeStructureCobblestone));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(powerAdapterItem, 4), "scs", "sbs", "sas", 's', pipeStructureCobblestone, 'a',
                Items.redstone, 'b', "gearStone", 'c', "ingotGold"));

        if (Loader.isModLoaded("BuildCraft|Silicon")) {
            TransportSiliconRecipes.loadSiliconRecipes();
        } else {
            BCLog.logger.warn("**********************************************");
            BCLog.logger.warn("*   You are using the BuildCraft Transport   *");
            BCLog.logger.warn("*  module WITHOUT the Silicon module. Gates  *");
            BCLog.logger.warn("*           will not be available.           *");
            BCLog.logger.warn("**********************************************");

            // Alternate recipes
            // Lenses, Filters
            for (int i = 0; i < 16; i++) {
                String dye = ColorUtils.getOreDictionaryName(15 - i);
                GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(lensItem, 8, i), "OSO", "SGS", "OSO", 'O', "ingotIron", 'S', dye, 'G',
                        "blockGlass"));
                GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(lensItem, 8, i + 16), "OSO", "SGS", "OSO", 'O', Blocks.iron_bars, 'S', dye,
                        'G', "blockGlass"));
            }
        }
    }

    public static void loadComplexRefiningRecipes() {
        for (PipeRecipe pipe : pipeRecipes) {
            Object[] newInput = new Object[pipe.input.length];
            System.arraycopy(pipe.input, 0, newInput, 0, pipe.input.length);
            boolean changed = false;
            for (int i = 0; i < newInput.length; i++) {
                Object o = newInput[i];
                if ("blockGlassColorless".equals(o)) {
                    newInput[i] = BuildCraftFactory.plasticSheetItem;
                    // changed = true;
                }
            }
            if (!changed) continue;
            if (pipe.isShapeless) {
                BCRegistry.INSTANCE.addShapelessRecipe(pipe.result, newInput);
            } else {
                BCRegistry.INSTANCE.addCraftingRecipe(pipe.result, newInput);
            }
        }
    }

    @Mod.EventHandler
    public void processIMCRequests(IMCEvent event) {
        InterModComms.processIMC(event);
    }

    public static Item buildPipe(Class<? extends Pipe<?>> clas, Object... ingredients) {
        return buildPipe(clas, BCCreativeTab.get("pipes"), ingredients);
    }

    @Deprecated
    public static Item buildPipe(Class<? extends Pipe<?>> clas, String descr, BCCreativeTab creativeTab, Object... ingredients) {
        return buildPipe(clas, creativeTab, ingredients);
    }

    public static Item buildPipe(Class<? extends Pipe<?>> clas, BCCreativeTab creativeTab, Object... ingredients) {
        if (!BCRegistry.INSTANCE.isEnabled("pipes", clas.getSimpleName())) {
            return null;
        }

        ItemPipe res = BlockGenericPipe.registerPipe(clas, creativeTab);
        res.setUnlocalizedName(clas.getSimpleName());

        for (Object o : ingredients) {
            if (o == null) {
                return res;
            }
        }

        // Add appropriate recipes to temporary list
        if (ingredients.length == 3) {
            for (int i = 0; i < 17; i++) {
                PipeRecipe recipe = new PipeRecipe();
                Object glass;

                if (i == 0) {
                    glass = ingredients[1];
                } else {
                    glass = "blockGlass" + EnumColor.fromId(15 - (i - 1)).getName();
                }

                recipe.result = new ItemStack(res, 8, i);
                recipe.input = new Object[] { "ABC", 'A', ingredients[0], 'B', glass, 'C', ingredients[2] };

                pipeRecipes.add(recipe);
            }
        } else if (ingredients.length == 2) {
            for (int i = 0; i < 17; i++) {
                PipeRecipe recipe = new PipeRecipe();

                Object left = ingredients[0];
                Object right = ingredients[1];

                if (ingredients[1] instanceof ItemPipe) {
                    right = new ItemStack((Item) right, 1, i);
                }

                recipe.isShapeless = true;
                recipe.result = new ItemStack(res, 1, i);
                recipe.input = new Object[] { left, right };

                pipeRecipes.add(recipe);

                if (ingredients[1] instanceof ItemPipe && clas != PipeStructureCobblestone.class) {
                    PipeRecipe uncraft = new PipeRecipe();
                    uncraft.isShapeless = true;
                    uncraft.input = new Object[] { recipe.result };
                    uncraft.result = (ItemStack) right;
                    pipeRecipes.add(uncraft);
                }
            }
        }

        return res;
    }

    @Mod.EventHandler
    public void whiteListAppliedEnergetics(FMLInitializationEvent event) {
        FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial", TileGenericPipe.class.getCanonicalName());
        FMLInterModComms.sendMessage("appliedenergistics2", "whitelist-spatial", TileFilteredBuffer.class.getCanonicalName());
    }

    @Mod.EventHandler
    public void remap(FMLMissingMappingsEvent event) {
        BCLog.logger.info("Transport|Remap " + System.identityHashCode(event));
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.getAll()) {
            String name = mapping.name;
            BCLog.logger.info("        - " + name);

            if (mapping.type == GameRegistry.Type.ITEM) {
                if (mapping.name.equals("BuildCraft|Transport:robotStation")) {
                    mapping.remap(Item.itemRegistry.getObject(new ResourceLocation("BuildCraft|Robotics:robotStation")));
                }
            }

            if (mapping.resourceLocation.getResourceDomain().equals("BuildCraft|Transport") && mapping.name.toLowerCase(Locale.ROOT).contains(
                    "pipe")) {
                mapping.remap(Item.itemRegistry.getObject(new ResourceLocation(mapping.name.replace("item.", ""))));
                BCLog.logger.info("          - remapped pipe");
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelBakeEvent event) {
        ModelResourceLocation mrl = new ModelResourceLocation("buildcrafttransport:pipeBlock");
        event.modelRegistry.putObject(mrl, new PipeBlockModel());

        PipeRendererFluids.INSTANCE.modelBake();

        for (ItemPipe itemPipe : BlockGenericPipe.pipes.keySet()) {
            for (int i = 0; i < 17; i++) {
                mrl = ModelHelper.getItemResourceLocation(itemPipe, "_" + i);
                event.modelRegistry.putObject(mrl, PipeItemModel.create(itemPipe, i));
            }
        }

        if (lensItem != null) {
            for (int i = 0; i < 34; i++) {
                mrl = ModelHelper.getItemResourceLocation(lensItem, "_" + i);
                event.modelRegistry.putObject(mrl, LensPluggableModel.create(lensItem, i));
            }
        }

        if (plugItem != null) {
            mrl = ModelHelper.getItemResourceLocation(plugItem, "");
            event.modelRegistry.putObject(mrl, PlugPluggableModel.create());
        }

        if (powerAdapterItem != null) {
            mrl = ModelHelper.getItemResourceLocation(powerAdapterItem, "");
            event.modelRegistry.putObject(mrl, PowerAdapterModel.create());
        }

        if (pipeGate != null) {
            mrl = ModelHelper.getItemResourceLocation(pipeGate, "");
            event.modelRegistry.putObject(mrl, GateItemModel.INSTANCE);
        }

        if (facadeItem != null) {
            mrl = ModelHelper.getItemResourceLocation(facadeItem, "");
            event.modelRegistry.putObject(mrl, FacadeItemModel.INSTANCE);
        }
    }
}

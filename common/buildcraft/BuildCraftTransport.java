/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft;

import java.io.PrintWriter;
import java.util.LinkedList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;

import buildcraft.api.blueprints.BuilderAPI;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumColor;
import buildcraft.api.facades.FacadeAPI;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.transport.ICustomPipeConnection;
import buildcraft.api.transport.PipeConnectionAPI;
import buildcraft.api.transport.PipeManager;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.CompatHooks;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.PowerMode;
import buildcraft.core.Version;
import buildcraft.core.config.ConfigManager;
import buildcraft.core.lib.items.ItemBuildCraft;
import buildcraft.core.lib.network.ChannelHandler;
import buildcraft.core.lib.network.PacketHandler;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.ModelHelper;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.*;
import buildcraft.transport.gates.GateDefinition;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import buildcraft.transport.gates.GateExpansionLightSensor;
import buildcraft.transport.gates.GateExpansionPulsar;
import buildcraft.transport.gates.GateExpansionRedstoneFader;
import buildcraft.transport.gates.GateExpansionTimer;
import buildcraft.transport.gates.GatePluggable;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.network.PacketFluidUpdate;
import buildcraft.transport.network.PacketPipeTransportItemStack;
import buildcraft.transport.network.PacketPipeTransportItemStackRequest;
import buildcraft.transport.network.PacketPipeTransportTraveler;
import buildcraft.transport.network.PacketPowerUpdate;
import buildcraft.transport.pipes.*;
import buildcraft.transport.pluggable.ItemLens;
import buildcraft.transport.pluggable.ItemPlug;
import buildcraft.transport.pluggable.LensPluggable;
import buildcraft.transport.pluggable.PlugPluggable;
import buildcraft.transport.render.PipeBlockModel;
import buildcraft.transport.render.PipeItemModel;
import buildcraft.transport.schematics.BptItemPipeFilters;
import buildcraft.transport.schematics.BptPipeIron;
import buildcraft.transport.schematics.BptPipeWooden;
import buildcraft.transport.schematics.SchematicPipe;
import buildcraft.transport.statements.*;
import buildcraft.transport.statements.ActionValve.ValveState;
import buildcraft.transport.statements.TriggerClockTimer.Time;
import buildcraft.transport.statements.TriggerPipeContents.PipeContents;
import buildcraft.transport.stripes.*;

@Mod(version = Version.VERSION, modid = "BuildCraft|Transport", name = "Buildcraft Transport", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftTransport extends BuildCraftMod {
    @Mod.Instance("BuildCraft|Transport")
    public static BuildCraftTransport instance;

    public static float pipeDurability;
    public static int pipeFluidsBaseFlowRate;
    public static boolean facadeTreatBlacklistAsWhitelist;
    public static boolean additionalWaterproofingRecipe;
    public static boolean facadeForceNonLaserRecipe;

    public static BlockGenericPipe genericPipeBlock;
    public static BlockFilteredBuffer filteredBufferBlock;

    public static Item pipeWaterproof;
    public static Item pipeGate;
    public static Item pipeWire;
    public static Item plugItem;
    public static Item lensItem;
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
        new BCCreativeTab("facades");
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
            BuildCraftCore.mainConfigManager.register("general.pipes.slimeballWaterproofRecipe", false,
                    "Should I enable an alternate Waterproof recipe, based on slimeballs?", ConfigManager.RestartRequirement.GAME);
            BuildCraftCore.mainConfigManager.register("power.gateCostMultiplier", 1.0D, "What should be the multiplier of all gate power costs?",
                    ConfigManager.RestartRequirement.GAME);
            BuildCraftCore.mainConfigManager.register("general.pipes.facadeBlacklist", new String[] { Utils.getNameForBlock(Blocks.bedrock), Utils
                    .getNameForBlock(Blocks.command_block), Utils.getNameForBlock(Blocks.end_portal_frame), Utils.getNameForBlock(Blocks.grass), Utils
                            .getNameForBlock(Blocks.leaves), Utils.getNameForBlock(Blocks.leaves2), Utils.getNameForBlock(Blocks.lit_pumpkin), Utils
                                    .getNameForBlock(Blocks.lit_redstone_lamp), Utils.getNameForBlock(Blocks.mob_spawner), Utils.getNameForBlock(
                                            Blocks.monster_egg), Utils.getNameForBlock(Blocks.redstone_lamp), Utils.getNameForBlock(
                                                    Blocks.double_stone_slab), Utils.getNameForBlock(Blocks.double_wooden_slab), Utils
                                                            .getNameForBlock(Blocks.sponge) },
                    "What block types should be blacklisted from being a facade?", ConfigManager.RestartRequirement.GAME);
            BuildCraftCore.mainConfigManager.register("general.pipes.facadeBlacklistAsWhitelist", false,
                    "Should the blacklist be treated as a whitelist instead?", ConfigManager.RestartRequirement.GAME);
            BuildCraftCore.mainConfigManager.register("general.pipes.facadeNoLaserRecipe", false,
                    "Should non-laser (crafting table) facade recipes be forced?", ConfigManager.RestartRequirement.GAME);

            reloadConfig(ConfigManager.RestartRequirement.GAME);

            filteredBufferBlock = new BlockFilteredBuffer();
            CoreProxy.proxy.registerBlock(filteredBufferBlock.setUnlocalizedName("filteredBufferBlock"));

            pipeWaterproof = new ItemBuildCraft();

            pipeWaterproof.setUnlocalizedName("pipeWaterproof");
            CoreProxy.proxy.registerItem(pipeWaterproof);

            genericPipeBlock = (BlockGenericPipe) CompatHooks.INSTANCE.getBlock(BlockGenericPipe.class);

            CoreProxy.proxy.registerBlock(genericPipeBlock.setUnlocalizedName("pipeBlock"), ItemBlock.class);

            BCCreativeTab pipeTab = BCCreativeTab.get("pipes");
            pipeItemsWood = buildPipe(PipeItemsWood.class, pipeTab, "plankWood", "blockGlassColorless", "plankWood");
            pipeItemsEmerald = buildPipe(PipeItemsEmerald.class, pipeTab, "gemEmerald", "blockGlassColorless", "gemEmerald");
            pipeItemsCobblestone = buildPipe(PipeItemsCobblestone.class, pipeTab, "cobblestone", "blockGlassColorless", "cobblestone");
            pipeItemsStone = buildPipe(PipeItemsStone.class, pipeTab, "stone", "blockGlassColorless", "stone");
            pipeItemsQuartz = buildPipe(PipeItemsQuartz.class, pipeTab, "blockQuartz", "blockGlassColorless", "blockQuartz");
            pipeItemsIron = buildPipe(PipeItemsIron.class, pipeTab, "ingotIron", "blockGlassColorless", "ingotIron");
            pipeItemsGold = buildPipe(PipeItemsGold.class, pipeTab, "ingotGold", "blockGlassColorless", "ingotGold");
            pipeItemsDiamond = buildPipe(PipeItemsDiamond.class, pipeTab, "gemDiamond", "blockGlassColorless", "gemDiamond");
            pipeItemsObsidian = buildPipe(PipeItemsObsidian.class, pipeTab, Blocks.obsidian, "blockGlassColorless", Blocks.obsidian);
            pipeItemsLapis = buildPipe(PipeItemsLapis.class, pipeTab, "blockLapis", "blockGlassColorless", "blockLapis");
            pipeItemsDaizuli = buildPipe(PipeItemsDaizuli.class, pipeTab, "blockLapis", "blockGlassColorless", "gemDiamond");
            pipeItemsSandstone = buildPipe(PipeItemsSandstone.class, pipeTab, Blocks.sandstone, "blockGlassColorless", Blocks.sandstone);
            pipeItemsVoid = buildPipe(PipeItemsVoid.class, pipeTab, "dyeBlack", "blockGlassColorless", "dustRedstone");
            pipeItemsEmzuli = buildPipe(PipeItemsEmzuli.class, pipeTab, "blockLapis", "blockGlassColorless", "gemEmerald");
            pipeItemsStripes = buildPipe(PipeItemsStripes.class, pipeTab, "gearGold", "blockGlassColorless", "gearGold");
            pipeItemsClay = buildPipe(PipeItemsClay.class, pipeTab, Blocks.clay, "blockGlassColorless", Blocks.clay);

            pipeFluidsWood = buildPipe(PipeFluidsWood.class, pipeTab, pipeWaterproof, pipeItemsWood);
            pipeFluidsCobblestone = buildPipe(PipeFluidsCobblestone.class, pipeTab, pipeWaterproof, pipeItemsCobblestone);
            pipeFluidsStone = buildPipe(PipeFluidsStone.class, pipeTab, pipeWaterproof, pipeItemsStone);
            pipeFluidsQuartz = buildPipe(PipeFluidsQuartz.class, pipeTab, pipeWaterproof, pipeItemsQuartz);
            pipeFluidsIron = buildPipe(PipeFluidsIron.class, pipeTab, pipeWaterproof, pipeItemsIron);
            pipeFluidsGold = buildPipe(PipeFluidsGold.class, pipeTab, pipeWaterproof, pipeItemsGold);
            pipeFluidsEmerald = buildPipe(PipeFluidsEmerald.class, pipeTab, pipeWaterproof, pipeItemsEmerald);
            pipeFluidsDiamond = buildPipe(PipeFluidsDiamond.class, pipeTab, pipeWaterproof, pipeItemsDiamond);
            pipeFluidsSandstone = buildPipe(PipeFluidsSandstone.class, pipeTab, pipeWaterproof, pipeItemsSandstone);
            pipeFluidsVoid = buildPipe(PipeFluidsVoid.class, pipeTab, pipeWaterproof, pipeItemsVoid);

            pipePowerWood = buildPipe(PipePowerWood.class, pipeTab, "dustRedstone", pipeItemsWood);
            pipePowerCobblestone = buildPipe(PipePowerCobblestone.class, pipeTab, "dustRedstone", pipeItemsCobblestone);
            pipePowerStone = buildPipe(PipePowerStone.class, pipeTab, "dustRedstone", pipeItemsStone);
            pipePowerQuartz = buildPipe(PipePowerQuartz.class, pipeTab, "dustRedstone", pipeItemsQuartz);
            pipePowerIron = buildPipe(PipePowerIron.class, pipeTab, "dustRedstone", pipeItemsIron);
            pipePowerGold = buildPipe(PipePowerGold.class, pipeTab, "dustRedstone", pipeItemsGold);
            pipePowerDiamond = buildPipe(PipePowerDiamond.class, pipeTab, "dustRedstone", pipeItemsDiamond);
            pipePowerEmerald = buildPipe(PipePowerEmerald.class, pipeTab, "dustRedstone", pipeItemsEmerald);
            pipePowerSandstone = buildPipe(PipePowerSandstone.class, pipeTab, "dustRedstone", pipeItemsSandstone);

            pipeStructureCobblestone = buildPipe(PipeStructureCobblestone.class, pipeTab, Blocks.gravel, pipeItemsCobblestone);

            pipeWire = new ItemPipeWire();
            CoreProxy.proxy.registerItem(pipeWire);
            PipeWire.item = pipeWire;

            pipeGate = new ItemGate();
            pipeGate.setUnlocalizedName("pipeGate");
            CoreProxy.proxy.registerItem(pipeGate);

            facadeItem = new ItemFacade();
            facadeItem.setUnlocalizedName("pipeFacade");
            CoreProxy.proxy.registerItem(facadeItem);
            FacadeAPI.facadeItem = facadeItem;

            plugItem = new ItemPlug();
            plugItem.setUnlocalizedName("pipePlug");
            CoreProxy.proxy.registerItem(plugItem);

            lensItem = new ItemLens();
            lensItem.setUnlocalizedName("pipeLens");
            CoreProxy.proxy.registerItem(lensItem);

            // powerAdapterItem = new ItemPowerAdapter();
            // powerAdapterItem.setUnlocalizedName("pipePowerAdapter");
            // CoreProxy.proxy.registerItem(powerAdapterItem);

            gateCopier = new ItemGateCopier();
            CoreProxy.proxy.registerItem(gateCopier);

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

            for (EnumColor color : EnumColor.VALUES) {
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
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        transportChannelHandler = ChannelHandler.createChannelHandler();
        MinecraftForge.EVENT_BUS.register(this);

        transportChannelHandler.registerPacketType(PacketFluidUpdate.class);
        transportChannelHandler.registerPacketType(PacketPipeTransportItemStack.class);
        transportChannelHandler.registerPacketType(PacketPipeTransportItemStackRequest.class);
        transportChannelHandler.registerPacketType(PacketPipeTransportTraveler.class);
        transportChannelHandler.registerPacketType(PacketPowerUpdate.class);

        channels = NetworkRegistry.INSTANCE.newChannel(DefaultProps.NET_CHANNEL_NAME + "-TRANSPORT", transportChannelHandler, new PacketHandler());

        TransportProxy.proxy.registerTileEntities();

        BuilderAPI.schematicRegistry.registerSchematicBlock(genericPipeBlock, SchematicPipe.class);

        new BptPipeIron(pipeItemsIron);
        new BptPipeIron(pipeFluidsIron);
        new BptPipeIron(pipePowerIron);

        new BptPipeWooden(pipeItemsWood);
        new BptPipeWooden(pipeFluidsWood);
        new BptPipeWooden(pipePowerWood);
        new BptPipeWooden(pipeItemsEmerald);

        new BptItemPipeFilters(pipeItemsDiamond);

        BCCreativeTab.get("pipes").setIcon(new ItemStack(BuildCraftTransport.pipeItemsDiamond, 1));
        BCCreativeTab.get("facades").setIcon(facadeItem.getFacadeForBlock(Blocks.brick_block.getDefaultState()));
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

        GateExpansions.registerExpansion(GateExpansionPulsar.INSTANCE);
        GateExpansions.registerExpansion(GateExpansionTimer.INSTANCE);
        GateExpansions.registerExpansion(GateExpansionRedstoneFader.INSTANCE);
        GateExpansions.registerExpansion(GateExpansionLightSensor.INSTANCE, new ItemStack(Blocks.daylight_detector));

        if (BuildCraftCore.loadDefaultRecipes) {
            loadRecipes();
        }

        TransportProxy.proxy.registerRenderers();
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new TransportGuiHandler());

        // Make pipes extend to connect to blocks like chests
        ICustomPipeConnection smallerBlockConnection = new ICustomPipeConnection() {
            @Override
            public float getExtension(World world, BlockPos pos, EnumFacing face, IBlockState state) {
                return face == EnumFacing.UP ? 0 : 2 / 16f;
            }
        };

        PipeConnectionAPI.registerConnection(Blocks.chest, smallerBlockConnection);
        PipeConnectionAPI.registerConnection(Blocks.trapped_chest, smallerBlockConnection);
        PipeConnectionAPI.registerConnection(Blocks.hopper, smallerBlockConnection);
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
            expansion.registerBlockOverlay(event.map);
        }

        TriggerParameterSignal.registerIcons(event);
        ActionParameterSignal.registerIcons(event);
    }

    @Mod.EventHandler
    public void serverLoading(FMLServerStartingEvent event) {
        pipeExtensionListener = new PipeExtensionListener();
        FMLCommonHandler.instance().bus().register(pipeExtensionListener);
    }

    @Mod.EventHandler
    public void serverUnloading(FMLServerStoppingEvent event) {
        // One last tick
        for (WorldServer w : DimensionManager.getWorlds()) {
            pipeExtensionListener.tick(new TickEvent.WorldTickEvent(Side.SERVER, TickEvent.Phase.END, w));
        }
        FMLCommonHandler.instance().bus().unregister(pipeExtensionListener);
        pipeExtensionListener = null;
    }

    public void loadRecipes() {
        // Add base recipe for pipe waterproof.
        GameRegistry.addShapelessRecipe(new ItemStack(pipeWaterproof, 1), new ItemStack(Items.dye, 1, 2));
        if (additionalWaterproofingRecipe) {
            GameRegistry.addShapelessRecipe(new ItemStack(pipeWaterproof, 1), new ItemStack(Items.slime_ball));
        }

        // Add pipe recipes
        for (PipeRecipe pipe : pipeRecipes) {
            if (pipe.isShapeless) {
                CoreProxy.proxy.addShapelessRecipe(pipe.result, pipe.input);
            } else {
                CoreProxy.proxy.addCraftingRecipe(pipe.result, pipe.input);
            }
        }

        GameRegistry.addRecipe(new PipeColoringRecipe());
        RecipeSorter.register("buildcraft:pipecoloring", PipeColoringRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");

        CoreProxy.proxy.addCraftingRecipe(new ItemStack(filteredBufferBlock, 1), "wdw", "wcw", "wpw", 'w', "plankWood", 'd',
                BuildCraftTransport.pipeItemsDiamond, 'c', Blocks.chest, 'p', Blocks.piston);

        // Facade turning helper
        GameRegistry.addRecipe(facadeItem.new FacadeRecipe());
        RecipeSorter.register("facadeTurningHelper", ItemFacade.FacadeRecipe.class, RecipeSorter.Category.SHAPELESS, "");

        // Pipe Plug
        GameRegistry.addShapelessRecipe(new ItemStack(plugItem, 4), new ItemStack(pipeStructureCobblestone));

        if (Loader.isModLoaded("BuildCraft|Silicon")) {
            TransportSiliconRecipes.loadSiliconRecipes();
        } else {
            BCLog.logger.warn("**********************************************");
            BCLog.logger.warn("*   You are using the BuildCraft Transport   *");
            BCLog.logger.warn("* module WITHOUT the Silicon module. Certain *");
            BCLog.logger.warn("* crafting recipes will be unavailable, and  *");
            BCLog.logger.warn("*   you are HIGHLY encouraged to either add  *");
            BCLog.logger.warn("* the module or add custom recipes for those *");
            BCLog.logger.warn("*              parts of the mod.             *");
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

    @Mod.EventHandler
    public void processIMCRequests(IMCEvent event) {
        InterModComms.processIMC(event);
    }

    public static Item buildPipe(Class<? extends Pipe<?>> clas, BCCreativeTab creativeTab, Object... ingredients) {
        ItemPipe res = BlockGenericPipe.registerPipe(clas, creativeTab);
        res.setUnlocalizedName(clas.getSimpleName());

        // Add appropriate recipes to temporary list
        if (ingredients.length == 3) {
            for (int i = 0; i < 17; i++) {
                PipeRecipe recipe = new PipeRecipe();
                Object glass;

                if (i == 0) {
                    glass = ingredients[1];
                } else {
                    glass = new ItemStack(Blocks.stained_glass, 1, i - 1);
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

                if (ingredients[1] instanceof ItemPipe) {
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
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.get()) {
            if (mapping.type == GameRegistry.Type.ITEM) {
                if (mapping.name.equals("BuildCraft|Transport:robotStation")) {
                    mapping.remap((Item) Item.itemRegistry.getObject("BuildCraft|Robotics:robotStation"));
                }
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelBakeEvent event) {
        ModelResourceLocation mrl = new ModelResourceLocation("buildcrafttransport:pipeBlock");
        event.modelRegistry.putObject(mrl, new PipeBlockModel());
        for (ItemPipe itemPipe : BlockGenericPipe.pipes.keySet()) {
            mrl = ModelHelper.getItemResourceLocation(itemPipe, "");
            event.modelRegistry.putObject(mrl, PipeItemModel.create(itemPipe));
            BCLog.logger.info("Registering " + itemPipe + " as " + mrl);
        }
    }
}

package buildcraft.energy;

import buildcraft.api.BCModules;
import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.fluid.BCFluidAttributes;
import buildcraft.lib.fluid.BCFluidBlock;
import buildcraft.lib.fluid.BCFluidRegistryContainer;
import buildcraft.lib.item.ItemBucketBC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.DispenseFluidContainer;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BCEnergyFluids {
    private static final DeferredRegister<Fluid> fluidRegister = DeferredRegister.create(ForgeRegistries.FLUIDS, BCEnergy.MODID);
    private static final DeferredRegister<Block> blockRegister = DeferredRegister.create(ForgeRegistries.BLOCKS, BCEnergy.MODID);
    private static final DeferredRegister<Item> bucketRegister = DeferredRegister.create(ForgeRegistries.ITEMS, BCEnergy.MODID);

    static {
        IEventBus bus = ((FMLModContainer) ModList.get().getModContainerById(BCEnergy.MODID).get()).getEventBus();
        blockRegister.register(bus);
        fluidRegister.register(bus);
        bucketRegister.register(bus);
    }

    public static RegistryObject<BCFluid.Source>[] crudeOil;
    /** All 3 fuels (no residue) */
    public static RegistryObject<BCFluid.Source>[] oilDistilled;
    /** The 3 heaviest components (fuelLight, fuelDense and oilResidue) */
    public static RegistryObject<BCFluid.Source>[] oilHeavy;
    /** The 2 lightest fuels (no dense fuel) */
    public static RegistryObject<BCFluid.Source>[] fuelMixedLight;
    /** The 2 heaviest fuels (no gaseous fuel) */
    public static RegistryObject<BCFluid.Source>[] fuelMixedHeavy;
    /** The 2 heaviest products (fuelDense and oilResidue) */
    public static RegistryObject<BCFluid.Source>[] oilDense;

    // End products in order from least to most dense
    public static RegistryObject<BCFluid.Source>[] fuelGaseous;
    public static RegistryObject<BCFluid.Source>[] fuelLight;
    public static RegistryObject<BCFluid.Source>[] fuelDense;
    public static RegistryObject<BCFluid.Source>[] oilResidue;

    public static RegistryObject<BCFluid.Source>[] tar;

    public static final List<RegistryObject<BCFluid.Source>> allStill = new ArrayList<>();
    public static final List<RegistryObject<BCFluid.Flowing>> allFlow = new ArrayList<>();
    public static final List<BCFluidRegistryContainer> allFluids = new ArrayList<>();

    public static int[][] data = { //@formatter:off
        // Tabular form of all the fluid values
        // density, viscosity, boil, spread,  tex_light,   tex_dark, sticky, flammable
        {      900,      2000,    3,      6, 0x50_50_50, 0x05_05_05,      1,         1 },// Crude Oil
        {     1200,      4000,    3,      4, 0x10_0F_10, 0x42_10_42,      1,         0 },// Residue
        {      850,      1800,    3,      6, 0xA0_8F_1F, 0x42_35_20,      1,         1 },// Heavy Oil
        {      950,      1600,    3,      5, 0x87_6E_77, 0x42_24_24,      1,         1 },// Dense Oil
        {      750,      1400,    2,      8, 0xE4_AF_78, 0xB4_7F_00,      0,         1 },// Distilled Oil
        {      600,       800,    2,      7, 0xFF_AF_3F, 0xE0_7F_00,      0,         1 },// Dense Fuel
        {      700,      1000,    2,      7, 0xF2_A7_00, 0xC4_87_00,      0,         1 },// Mixed Heavy Fuels
        {      400,       600,    1,      8, 0xFF_FF_30, 0xE4_CF_00,      0,         1 },// Light Fuel
        {      650,       900,    1,      9, 0xF6_D7_00, 0xC4_B7_00,      0,         1 },// Mixed Light Fuels
        {      300,       500,    0,     10, 0xFA_F6_30, 0xE0_D9_00,      0,         1 },// Gas Fuel
    };//@formatter:on

    public static String STILL_SUFFIX = "_still";
    public static String FLOW_SUFFIX = "_flow";

    public static String FLUID_TRANSLATION_PREFIX = "buildcraft.fluid.heat_";
    public static String HEAT_TRANSLATION_PREFIX = "fluid.";
    public static boolean allowGas = true;

    public static void preInit() {
        if (BCModules.FACTORY.isLoaded()) {
            int index = 0;

            // Add all of the fluid states
            crudeOil = defineFluids(data[index++], "oil");
            oilResidue = defineFluids(data[index++], "oil_residue");
            oilHeavy = defineFluids(data[index++], "oil_heavy");
            oilDense = defineFluids(data[index++], "oil_dense");
            oilDistilled = defineFluids(data[index++], "oil_distilled");
            fuelDense = defineFluids(data[index++], "fuel_dense");
            fuelMixedHeavy = defineFluids(data[index++], "fuel_mixed_heavy");
            fuelLight = defineFluids(data[index++], "fuel_light");
            fuelMixedLight = defineFluids(data[index++], "fuel_mixed_light");
            fuelGaseous = defineFluids(data[index++], "fuel_gaseous");
        } else {
            crudeOil = new RegistryObject[] { defineFluid(data[0], 0, "oil") };
            oilResidue = new RegistryObject[0];
            oilHeavy = new RegistryObject[0];
            oilDense = new RegistryObject[0];
            oilDistilled = new RegistryObject[0];
            fuelDense = new RegistryObject[0];
            fuelMixedHeavy = new RegistryObject[0];
            fuelLight = new RegistryObject[] { defineFluid(data[7], 0, "fuel_light") };
            fuelMixedLight = new RegistryObject[0];
            fuelGaseous = new RegistryObject[0];
        }
    }

    private static RegistryObject<BCFluid.Source>[] defineFluids(int[] data, String name) {
        RegistryObject<BCFluid.Source>[] arr = new RegistryObject[3];
        for (int h = 0; h < 3; h++) {
            arr[h] = defineFluid(data, h, name);
        }
        return arr;
    }

    private static RegistryObject<BCFluid.Source> defineFluid(int[] data, int heat, String name) {
        final int density = data[0];
        final int baseViscosity = data[1];
        final int boilPoint = data[2];
        final int baseQuanta = data[3];
        final int texLight = data[4];
        final int texDark = data[5];
        final boolean sticky = BCEnergyConfig.oilIsSticky && data[6] == 1;
        final boolean flammable = BCEnergyConfig.enableOilBurn ? data[7] == 1 : false;

        String fullName = name + "_heat_" + heat;
        int tempAdjustedViscosity = baseViscosity * (4 - heat) / 4;
        int boilAdjustedDensity = density * (heat >= boilPoint ? -1 : 1);
        // Calen 1.20.1: isGaseous is defined by density in FluidType#isLighterThanAir()
        // def.setGaseous(def.getDensity() < 0)
        if (boilAdjustedDensity < 0 && !allowGas) {
            boilAdjustedDensity = 1;
        }

//        String fluidTexture = "buildcraftenergy:fluids/" + fullName;
        String fluidTexture = "buildcraftenergy:block/fluid/" + fullName;
        BCFluidRegistryContainer fluidRegistryContainer = new BCFluidRegistryContainer();
        allFluids.add(fluidRegistryContainer);

        // Properties
        ForgeFlowingFluid.Properties fluidProperties = new ForgeFlowingFluid.Properties(
                fluidRegistryContainer::getFluidType,
                fluidRegistryContainer::getStill,
                fluidRegistryContainer::getFlowing
        )
                .bucket(fluidRegistryContainer::getBucket)
                .block(fluidRegistryContainer::getBlock)
                // 1.12.2 BlockFluidBase#<init>: this.tickRate = fluid.viscosity / 200
                // 1.18.2: should set by ourselves
                .tickRate(tempAdjustedViscosity / 200)
                // Distance that the fluid will travel: 1->16
                // Higher heat values travel a little further
                // 1.12.2: block.setQuantaPerBlock(baseQuanta + (baseQuanta > 6 ? heat : heat / 2));
                // 1.12.2 max = 16 Block#setQuantaPerBlock(range)
                // 1.18.2 max = 8 Properties#levelDecreasePerBlock(decrease)
                .levelDecreasePerBlock(8 / Math.min((baseQuanta + (baseQuanta > 6 ? heat : heat / 2)) / 2, 8));

        // Attributes
        BCFluidAttributes.Builder attributeBuilder = BCFluidAttributes.builder(
                        new ResourceLocation(fluidTexture + STILL_SUFFIX),
                        new ResourceLocation(fluidTexture + FLOW_SUFFIX)
                )
                // def.setHeat(heat)
                .setHeat(heat)
                // def.setHeatable(true)
                .setHeatable(true)
                // def.setColour(texLight, texDark)
                .setColour(texLight, texDark)
                // Calen: if use color filter on white texture of water, here should be Water Overlay
                // if the texture is colored, [.overlay(WATER_OVERLAY) and .color(0xFFFFFFFF)] will make the fluid block looks white behind glass block
                .overlay(null)
//                // 1.12.2: BCFluid#colour
//                .color(0xFFFFFFFF)
                // def.setUnlocalizedName(name)
                .descriptionId(HEAT_TRANSLATION_PREFIX + name)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                // def.setTemperature(300 + 20 * heat)
                .temperature(300 + 20 * heat)
                // def.setDensity(boilAdjustedDensity)
                .density(boilAdjustedDensity)
                // def.setViscosity(tempAdjustedViscosity)
                .viscosity(tempAdjustedViscosity);
        fluidRegistryContainer.setFluidType(attributeBuilder.build());

        // Still
        String stillId = fullName;
        RegistryObject<BCFluid.Source> still = fluidRegister.register(
                stillId,
                () ->
                {
                    BCFluid.Source def = new BCFluid.Source(
                            fluidProperties,
                            fluidRegistryContainer
                    );
                    return def;
                }
        );
        fluidRegistryContainer.setStill(still);
        allStill.add(still);

        // Flow
        String flowId = fullName + "_flow";
        RegistryObject<BCFluid.Flowing> flow = fluidRegister.register(
                flowId,
                () -> new BCFluid.Flowing(fluidProperties, fluidRegistryContainer)
        );
        fluidRegistryContainer.setFlowing(flow);
        allFlow.add(flow);

        // Bucket
        String bucketId = fullName + "_bucket";
        Item.Properties bucketProp = new Item.Properties()
                .stacksTo(1)
                .craftRemainder(Items.BUCKET);
        RegistryObject<ItemBucketBC> bucket = bucketRegister.register(
                bucketId,
                () -> new ItemBucketBC(
                        fluidRegistryContainer::getStill,
                        bucketProp
                )
        );
        fluidRegistryContainer.setBucket(bucket);

        // Block
        String blockId = "fluid_block_" + name + "_heat_" + heat;
        BlockBehaviour.Properties blockProp = BlockBehaviour.Properties.of()
                .liquid()
                .forceSolidOff()
                .pushReaction(PushReaction.DESTROY)
                // def.setMapColour(getMapColor(texDark))
                .mapColor(getMapColor(texDark))
                .noCollission()
                .randomTicks()
                .strength(100.0F)
                .noLootTable();
        if (flammable) {
            // def.setFlammable(flammable)
            blockProp.ignitedByLava();
        }
        RegistryObject<BCFluidBlock> block = blockRegister.register(
                blockId,
                () -> new BCFluidBlock(
                        fluidRegistryContainer::getStill,
                        blockProp,
                        // block.setSticky(sticky)
                        sticky,
                        fluidRegistryContainer
                )
        );
        fluidRegistryContainer.setBlock(block);

        // TODO Calen setLightOpacity???
//        block.setLightOpacity(3);
        return still;
    }

    // private static MapColor getMapColor(int color)
    private static MapColor getMapColor(int color) {
//        MapColor bestMapColor = MapColor.BLACK;
        MapColor bestMapColor = MapColor.COLOR_BLACK;
        int currentDifference = Integer.MAX_VALUE;

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

//        for (MapColor mapColor : MapColor.COLORS)
        for (MapColor mapColor : MapColor.MATERIAL_COLORS) {
//            if (mapColor == null || mapColor.colorValue == 0)
            if (mapColor == null || mapColor.col == 0) {
                continue;
            }
//            int mr = (mapColor.colorValue >> 16) & 0xFF;
            int mr = (mapColor.col >> 16) & 0xFF;
//            int mg = (mapColor.colorValue >> 8) & 0xFF;
            int mg = (mapColor.col >> 8) & 0xFF;
//            int mb = mapColor.colorValue & 0xFF;
            int mb = mapColor.col & 0xFF;

            int dr = mr - r;
            int dg = mg - g;
            int db = mb - b;

            int difference = dr * dr + dg * dg + db * db;

            if (difference < currentDifference) {
                currentDifference = difference;
                bestMapColor = mapColor;
            }
        }
        return bestMapColor;
    }

    public static List<RegistryObject<BCFluid.Source>> getAllStill() {
        return Collections.unmodifiableList(allStill);
    }

    public static List<RegistryObject<BCFluid.Flowing>> getAllFlow() {
        return Collections.unmodifiableList(allFlow);
    }

    public static void registerBucketDispenserBehavior() {
        for (RegistryObject<BCFluid.Source> fluid : getAllStill()) {
            DispenserBlock.registerBehavior(fluid.get().getBucket(), DispenseFluidContainer.getInstance());
        }
    }
}

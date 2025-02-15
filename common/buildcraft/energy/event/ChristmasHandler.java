package buildcraft.energy.event;

import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.registry.TagManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.registries.RegistryObject;

import java.time.Month;
import java.time.MonthDay;

/** Used for automatically changing lang entries, fluid colours, and a few other things around christmas time. This is
 * in energy rather than lib because no other module does anything at christmas. */
public class ChristmasHandler {

    private static Boolean enabled;

    public static boolean isEnabled() {
        if (enabled == null) {
            throw new IllegalStateException("Unknown until init!");
        }
        return enabled;
    }

    public static final int[][] colours =
            {
                    { 0xC0_75_34, 0x5A_1D_0c },
                    { 0xD4_82_39, 0xD8_7D_33 },
                    { 0xD4_82_39, 0x5A_1D_0C },
                    { 0xD4_82_39, 0x30_0E_05 },
                    { 0xC0_75_34, 0x8a_3D_1C },
                    { 0x4F_33_2F, 0x30_0E_05 },
                    { 0x88_44_2D, 0x5A_1d_0C },
                    { 0x9B_61_39, 0x94_59_31 },
                    { 0xC0_75_34, 0xB3_68_2C },
                    { 0xD6_C9_90, 0xCF_BF_8E },
            };

    private static void fmlPreInit() {
        enabled = BCEnergyConfig.christmasEventStatus.isEnabled(MonthDay.of(Month.DECEMBER, 25));
        if (isEnabled()) {
//            setColours(0xC0_75_34, 0x5A_1D_0c, BCEnergyFluids.crudeOil);
//            setColours(0xD4_82_39, 0xD8_7D_33, BCEnergyFluids.oilResidue);
//            setColours(0xD4_82_39, 0x5A_1D_0C, BCEnergyFluids.oilHeavy);
//            setColours(0xD4_82_39, 0x30_0E_05, BCEnergyFluids.oilDense);
//            setColours(0xC0_75_34, 0x8a_3D_1C, BCEnergyFluids.oilDistilled);
//            setColours(0x4F_33_2F, 0x30_0E_05, BCEnergyFluids.fuelDense);
//            setColours(0x88_44_2D, 0x5A_1d_0C, BCEnergyFluids.fuelMixedHeavy);
//            setColours(0x9B_61_39, 0x94_59_31, BCEnergyFluids.fuelLight);
//            setColours(0xC0_75_34, 0xB3_68_2C, BCEnergyFluids.fuelMixedLight);
//            setColours(0xD6_C9_90, 0xCF_BF_8E, BCEnergyFluids.fuelGaseous);

            BCEnergyFluids.STILL_SUFFIX = BCEnergyFluids.STILL_SUFFIX + "_christmas";
            BCEnergyFluids.FLOW_SUFFIX = BCEnergyFluids.FLOW_SUFFIX + "_christmas";
            BCEnergyFluids.FLUID_TRANSLATION_PREFIX = "buildcraft.christmas." + BCEnergyFluids.FLUID_TRANSLATION_PREFIX;
            BCEnergyFluids.HEAT_TRANSLATION_PREFIX = "buildcraft.christmas." + BCEnergyFluids.HEAT_TRANSLATION_PREFIX;

            TagManager.getTag("block.engine.bc.iron")
                    .locale(
                            "buildcraft.christmas." +
                                    TagManager.getTag("block.engine.bc.iron").getSingleTag(TagManager.EnumTagType.UNLOCALIZED_NAME)
                    );

            // boilPoint -> no gas
            BCEnergyFluids.allowGas = false;
            for (int index = 0; index < BCEnergyFluids.data.length; index++) {
                changeData(index);
            }
        }
    }

    public static void fmlPreInitDedicatedServer() {
        fmlPreInit();
//        if (isEnabled()) {
//            replaceLangEntries();
//        }
    }

    public static void fmlPreInitClient() {
        fmlPreInit();
//        if (isEnabled()) {
//            ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
//                    .registerReloadListener(m -> replaceLangEntries());
//        }
    }

    // private static void setColours(int lightColour, int darkColour, BCFluid[] fluids)
    private static void changeData(int fluidIndex) {
//        if (fluids != null) {
//            for (BCFluid fluid : fluids) {
//                fluid.setColour(lightColour, darkColour);
//                if (fluid.isGaseous()) {
//                    fluid.setGaseous(false);
//                }
//                if (fluid.getDensity() < 0) {
//                    fluid.setDensity(-fluid.getDensity());
//                }
//            }
//        }
        // density
        BCEnergyFluids.data[fluidIndex][0] = -BCEnergyFluids.data[fluidIndex][0];
    }

    public static void regBucketNoFlipModel(ModelEvent.RegisterAdditional event) {
        if (isEnabled()) {
            for (RegistryObject<BCFluid.Source> fluid : BCEnergyFluids.allStill) {
                ResourceLocation bucketRegRL = fluid.get().getReg().getBucket().getRegistryName();
                String namespace = bucketRegRL.getNamespace();
                String normalPath = bucketRegRL.getPath();
                String christmasPath = normalPath + "_christmas";
                ResourceLocation christmasModelRL = new ResourceLocation(namespace, "item/" + christmasPath);
//                ForgeModelBakery.addSpecialModel(christmasModelRL);
                event.register(christmasModelRL);
            }
        }
    }

    public static void replaceBucketNoFlipModel(ModelEvent.ModifyBakingResult event) {
        if (isEnabled()) {
            for (RegistryObject<BCFluid.Source> fluid : BCEnergyFluids.allStill) {
                ResourceLocation normalBucketRegRL = fluid.get().getReg().getBucket().getRegistryName();
                String namespace = normalBucketRegRL.getNamespace();
                String normalPath = normalBucketRegRL.getPath();
                String christmasPath = normalPath + "_christmas";
                ResourceLocation normalModelRL = new ResourceLocation(namespace, "item/" + normalPath);
                ResourceLocation christmasModelRL = new ResourceLocation(namespace, "item/" + christmasPath);
                event.getModels().replace(normalModelRL, event.getModels().get(christmasModelRL));
            }
        }
    }

//    private static void replaceLangEntries() {
//        try {
//            replaceLangEntries0();
//        } catch (ReflectiveOperationException e) {
//            BCLog.logger.warn("[energy.christmas] Unable to replace language entries! Did something change?", e);
//        }
//    }

//    private static void replaceLangEntries0() throws ReflectiveOperationException {
//////        Class<?> cls = LanguageMap.class;
////        Class<?> cls = LanguageHook.class;
////        Field fldInstance = null, fldLangMap = null;
////        for (Field fld : cls.getDeclaredFields()) {
////            if (fld.getType() == cls) {
////                if (fldInstance == null) {
////                    fldInstance = fld;
////                } else {
////                    throw new ReflectiveOperationException(
////                        "Found duplicate fields for instance! (" + fldInstance + " and " + fld + ")");
////                }
////            } else if (fld.getType() == Map.class) {
////                if (fldLangMap == null) {
////                    fldLangMap = fld;
////                } else {
////                    throw new ReflectiveOperationException(
////                        "Found duplicate fields for langMap! (" + fldLangMap + " and " + fld + ")");
////                }
////            }
////        }
////        if (fldInstance == null) {
////            throw new ReflectiveOperationException("Couln't find the instance field!");
////        }
////        if (fldLangMap == null) {
////            throw new ReflectiveOperationException("Couln't find the map field!");
////        }
////        fldInstance.setAccessible(true);
////        fldLangMap.setAccessible(true);
////
////        LanguageMap instance = (LanguageMap) fldInstance.get(null);
////        // never cast to a Map<String, String> as a mod
////        // might change it with bytecode manipulation
////        // Fortunately we can just replace the entry ourselves,
////        // As Map.get() takes an object, not a generic value.
////        checkAndReplaceEntries((Map<?, ?>) fldLangMap.get(instance));
//        checkAndReplaceEntries(LanguageHook.modTable);
//
////        fldInstance.setAccessible(false);
////        fldLangMap.setAccessible(false);
//    }

//    private static <K, V> void checkAndReplaceEntries(Map<K, V> map) {
//        for (Map.Entry<K, V> entry : map.entrySet()) {
//            K key = entry.getKey();
//            V altValue = map.get("buildcraft.christmas." + key);
//            if (altValue != null) {
//                entry.setValue(altValue);
//            }
//        }
//    }
}

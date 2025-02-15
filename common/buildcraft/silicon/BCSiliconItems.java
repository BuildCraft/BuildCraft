package buildcraft.silicon;

import buildcraft.api.enums.EnumRedstoneChipset;
import buildcraft.api.facades.FacadeAPI;
import buildcraft.api.facades.IFacadeItem;
import buildcraft.lib.item.ItemPluggableSimple;
import buildcraft.lib.item.ItemPropertiesCreator;
import buildcraft.lib.registry.RegistrationHelper;
import buildcraft.lib.registry.TagManager;
import buildcraft.silicon.gate.EnumGateLogic;
import buildcraft.silicon.gate.EnumGateMaterial;
import buildcraft.silicon.gate.EnumGateModifier;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.item.*;
import buildcraft.silicon.plug.PluggablePulsar;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class BCSiliconItems {

    private static final RegistrationHelper HELPER = new RegistrationHelper(BCSilicon.MODID);

    public static RegistryObject<ItemRedstoneChipset> chipsetRedstone;
    public static RegistryObject<ItemRedstoneChipset> chipsetIron;
    public static RegistryObject<ItemRedstoneChipset> chipsetGold;
    public static RegistryObject<ItemRedstoneChipset> chipsetQuartz;
    public static RegistryObject<ItemRedstoneChipset> chipsetDiamond;

    public static RegistryObject<ItemGateCopier> gateCopier;

    // public static RegistryObject<ItemPluggableGate> plugGate;
    public static final Map<GateVariant, RegistryObject<ItemPluggableGate>> variantGateMap = new HashMap<>();
    public static RegistryObject<ItemPluggableLens> plugLens;
    public static RegistryObject<ItemPluggableSimple> plugPulsar;
    public static RegistryObject<ItemPluggableSimple> plugLightSensor;
    public static RegistryObject<? extends IFacadeItem> plugFacade;


    public static void preInit() {
        chipsetRedstone = HELPER.addItem("item.chipset.redstone", ItemPropertiesCreator.common64(), (idBC, properties) -> new ItemRedstoneChipset(idBC, properties, EnumRedstoneChipset.RED));
        chipsetIron = HELPER.addItem("item.chipset.iron", ItemPropertiesCreator.common64(), (idBC, properties) -> new ItemRedstoneChipset(idBC, properties, EnumRedstoneChipset.IRON));
        chipsetGold = HELPER.addItem("item.chipset.gold", ItemPropertiesCreator.common64(), (idBC, properties) -> new ItemRedstoneChipset(idBC, properties, EnumRedstoneChipset.GOLD));
        chipsetQuartz = HELPER.addItem("item.chipset.quartz", ItemPropertiesCreator.common64(), (idBC, properties) -> new ItemRedstoneChipset(idBC, properties, EnumRedstoneChipset.QUARTZ));
        chipsetDiamond = HELPER.addItem("item.chipset.diamond", ItemPropertiesCreator.common64(), (idBC, properties) -> new ItemRedstoneChipset(idBC, properties, EnumRedstoneChipset.DIAMOND));

        gateCopier = HELPER.addItem("item.gate_copier", ItemPropertiesCreator.common1(), ItemGateCopier::new);

        // Gates
//        plugGate = HELPER.addItem("item.plug.gate", ItemPropertiesCreator.common64(), ItemPluggableGate::new);
        GateVariant gateVariant = new GateVariant(new CompoundTag());
        String registryId = TagManager.getTag("item.plug.gate", TagManager.EnumTagType.REGISTRY_NAME).replace(BCSilicon.MODID + ":", "");
        RegistryObject<ItemPluggableGate> plug = HELPER.addItem("item.plug.gate", registryId, ItemPropertiesCreator.common64(), (idBC, prop) -> new ItemPluggableGate(idBC, prop, gateVariant));
        variantGateMap.put(gateVariant, plug);
        for (EnumGateMaterial material : EnumGateMaterial.VALUES) {
            if (!material.canBeModified) {
                continue;
            }
            for (EnumGateLogic logic : EnumGateLogic.VALUES) {
                for (EnumGateModifier modifier : EnumGateModifier.VALUES) {
                    GateVariant gateVariant_i = new GateVariant(logic, material, modifier);
                    String registryId_i = TagManager.getTag("item.plug.gate", TagManager.EnumTagType.REGISTRY_NAME).replace(BCSilicon.MODID + ":", "") + "_" + gateVariant_i.getVariantName();
                    RegistryObject<ItemPluggableGate> plug_i = HELPER.addItem("item.plug.gate", registryId_i, ItemPropertiesCreator.common64(), (idBC, prop) -> new ItemPluggableGate(idBC, prop, gateVariant_i));
                    variantGateMap.put(gateVariant_i, plug_i);
                }
            }
        }

        plugLens = HELPER.addItem("item.plug.lens", ItemPropertiesCreator.common64(), ItemPluggableLens::new);
        plugPulsar = HELPER.addItem("item.plug.pulsar", ItemPropertiesCreator.common64(), (id, p) -> new ItemPluggableSimple(id, p, BCSiliconPlugs.pulsar,
                PluggablePulsar::new, ItemPluggableSimple.PIPE_BEHAVIOUR_ACCEPTS_RS_POWER));
        plugLightSensor = HELPER.addItem("item.plug.light_sensor", ItemPropertiesCreator.common64(), (id, p) -> new ItemPluggableSimple(id, p, BCSiliconPlugs.lightSensor));
        plugFacade = HELPER.addItem("item.plug.facade", ItemPropertiesCreator.common64(), ItemPluggableFacade::new);
        FacadeAPI.facadeItem = plugFacade;
    }
}

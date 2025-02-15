package buildcraft.transport;

import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.item.ItemPluggableSimple;
import buildcraft.lib.item.ItemPropertiesCreator;
import buildcraft.lib.registry.RegistrationHelper;
import buildcraft.transport.item.ItemWire;
import buildcraft.transport.pipe.PipeRegistry;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

public class BCTransportItems {
    private static final RegistrationHelper HELPER = new RegistrationHelper(BCTransport.MODID);

    public static RegistryObject<ItemBC_Neptune> waterproof;

    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeStructure;

    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemWood;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeFluidWood;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipePowerWood;

    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemStone;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeFluidStone;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipePowerStone;

    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemCobble;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeFluidCobble;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipePowerCobble;

    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemQuartz;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeFluidQuartz;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipePowerQuartz;

    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemGold;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeFluidGold;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipePowerGold;

    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemSandstone;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeFluidSandstone;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipePowerSandstone;

    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemIron;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeFluidIron;
    // public static ItemPipeHolder pipePowerIron;

    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemDiamond;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeFluidDiamond;
    // public static ItemPipeHolder pipePowerDiamond;

    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemDiaWood;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeFluidDiaWood;

    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemClay;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeFluidClay;

    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemVoid;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeFluidVoid;

    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemObsidian;
//    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeFluidObsidian;

    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemLapis;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemDaizuli;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemEmzuli;
    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> pipeItemStripes;

    public static RegistryObject<ItemPluggableSimple> plugBlocker;
    public static RegistryObject<ItemPluggableSimple> plugPowerAdaptor;

    public static RegistryObject<ItemWire> wire;

    public static void preInit() {
        waterproof = HELPER.addItem("item.waterproof", ItemPropertiesCreator.common64(), ItemBC_Neptune::new);

        pipeStructure = makePipeItem(BCTransportPipes.structure);

        // Register them in order of type -- item, fluid, power
        pipeItemWood = makePipeItem(BCTransportPipes.woodItem);
        pipeItemCobble = makePipeItem(BCTransportPipes.cobbleItem);
        pipeItemStone = makePipeItem(BCTransportPipes.stoneItem);
        pipeItemQuartz = makePipeItem(BCTransportPipes.quartzItem);
        pipeItemIron = makePipeItem(BCTransportPipes.ironItem);
        pipeItemGold = makePipeItem(BCTransportPipes.goldItem);
        pipeItemClay = makePipeItem(BCTransportPipes.clayItem);
        pipeItemSandstone = makePipeItem(BCTransportPipes.sandstoneItem);
        pipeItemVoid = makePipeItem(BCTransportPipes.voidItem);
        pipeItemObsidian = makePipeItem(BCTransportPipes.obsidianItem);
        pipeItemDiamond = makePipeItem(BCTransportPipes.diamondItem);
        pipeItemDiaWood = makePipeItem(BCTransportPipes.diaWoodItem);
        pipeItemLapis = makePipeItem(BCTransportPipes.lapisItem);
        pipeItemDaizuli = makePipeItem(BCTransportPipes.daizuliItem);
        pipeItemEmzuli = makePipeItem(BCTransportPipes.emzuliItem);
        pipeItemStripes = makePipeItem(BCTransportPipes.stripesItem);

        pipeFluidWood = makePipeItem(BCTransportPipes.woodFluid);
        pipeFluidCobble = makePipeItem(BCTransportPipes.cobbleFluid);
        pipeFluidStone = makePipeItem(BCTransportPipes.stoneFluid);
        pipeFluidQuartz = makePipeItem(BCTransportPipes.quartzFluid);
        pipeFluidGold = makePipeItem(BCTransportPipes.goldFluid);
        pipeFluidIron = makePipeItem(BCTransportPipes.ironFluid);
        pipeFluidClay = makePipeItem(BCTransportPipes.clayFluid);
        pipeFluidSandstone = makePipeItem(BCTransportPipes.sandstoneFluid);
        pipeFluidVoid = makePipeItem(BCTransportPipes.voidFluid);
        pipeFluidDiamond = makePipeItem(BCTransportPipes.diamondFluid);
        pipeFluidDiaWood = makePipeItem(BCTransportPipes.diaWoodFluid);
        // pipeFluidObsidian = makePipeItem(BCTransportPipes.obsidianFluid);

        pipePowerWood = makePipeItem(BCTransportPipes.woodPower);
        pipePowerCobble = makePipeItem(BCTransportPipes.cobblePower);
        pipePowerStone = makePipeItem(BCTransportPipes.stonePower);
        pipePowerQuartz = makePipeItem(BCTransportPipes.quartzPower);
        pipePowerGold = makePipeItem(BCTransportPipes.goldPower);
        // pipePowerIron = makePipeItem(BCTransportPipes.ironPower);
        pipePowerSandstone = makePipeItem(BCTransportPipes.sandstonePower);

        plugBlocker = HELPER.addItem("item.plug.blocker", ItemPropertiesCreator.common64(),
                (idBC, properties) -> new ItemPluggableSimple(idBC, properties, BCTransportPlugs.blocker)
        );
        plugPowerAdaptor = HELPER.addItem("item.plug.power_adaptor", ItemPropertiesCreator.common64(),
                (idBC, properties) -> new ItemPluggableSimple(idBC, properties, BCTransportPlugs.powerAdaptor, ItemPluggableSimple.PIPE_BEHAVIOUR_ACCEPTS_RS_POWER)
        );

        wire = HELPER.addItem("item.wire", ItemPropertiesCreator.common64(), ItemWire::new);
    }

    public static Map<DyeColor, RegistryObject<? extends IItemPipe>> makePipeItem(PipeDefinition def) {
        return PipeRegistry.INSTANCE.createItemForPipe(def);
    }
}

package buildcraft.transport;

import buildcraft.api.transport.neptune.PipeDefinition;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.item.ItemManager;
import buildcraft.transport.item.*;

public class BCTransportItems {
    public static ItemBC_Neptune waterproof;

    public static ItemPipeHolder pipeStructure;

    public static ItemPipeHolder pipeItemWood;
    public static ItemPipeHolder pipeFluidWood;
    public static ItemPipeHolder pipePowerWood;

    public static ItemPipeHolder pipeItemStone;
    public static ItemPipeHolder pipeFluidStone;
    public static ItemPipeHolder pipePowerStone;

    public static ItemPipeHolder pipeItemCobble;
    public static ItemPipeHolder pipeFluidCobble;
    public static ItemPipeHolder pipePowerCobble;

    public static ItemPipeHolder pipeItemQuartz;
    public static ItemPipeHolder pipeFluidQuartz;
    public static ItemPipeHolder pipePowerQuartz;

    public static ItemPipeHolder pipeItemGold;
    public static ItemPipeHolder pipeFluidGold;
    public static ItemPipeHolder pipePowerGold;

    public static ItemPipeHolder pipeItemSandstone;
    public static ItemPipeHolder pipeFluidSandstone;
    public static ItemPipeHolder pipePowerSandstone;

    public static ItemPipeHolder pipeItemIron;
    public static ItemPipeHolder pipeFluidIron;
    // public static ItemPipeHolder pipePowerIron;

    public static ItemPipeHolder pipeItemClay;
    public static ItemPipeHolder pipeFluidClay;

    public static ItemPipeHolder pipeItemVoid;
    public static ItemPipeHolder pipeFluidVoid;

    public static ItemPipeHolder pipeItemObsidian;
    public static ItemPipeHolder pipeFluidObsidian;

    public static ItemPipeHolder pipeItemLapis;
    public static ItemPipeHolder pipeItemDaizuli;

    public static ItemPluggableSimple plugBlocker;
    public static ItemPluggableGate plugGate;
    public static ItemPluggablePulsar plugPulsar;

    public static ItemWire wire;

    public static void preInit() {
        waterproof = ItemManager.register(new ItemBC_Neptune("item.waterproof"));

        // Register them in order of type -- item, fluid, power

        pipeStructure = makePipeItem(BCTransportPipes.structure);

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
        pipeItemLapis = makePipeItem(BCTransportPipes.lapisItem);
        pipeItemDaizuli = makePipeItem(BCTransportPipes.daizuliItem);

        pipeFluidWood = makePipeItem(BCTransportPipes.woodFluid);
        pipeFluidCobble = makePipeItem(BCTransportPipes.cobbleFluid);
        pipeFluidStone = makePipeItem(BCTransportPipes.stoneFluid);
        pipeFluidQuartz = makePipeItem(BCTransportPipes.quartzFluid);
        pipeFluidGold = makePipeItem(BCTransportPipes.goldFluid);
        pipeFluidIron = makePipeItem(BCTransportPipes.ironFluid);
        pipeFluidClay = makePipeItem(BCTransportPipes.clayFluid);
        pipeFluidSandstone = makePipeItem(BCTransportPipes.sandstoneFluid);
        pipeFluidVoid = makePipeItem(BCTransportPipes.voidFluid);
        // pipeFluidObsidian = makePipeItem(BCTransportPipes.obsidianFluid);

        pipePowerWood = makePipeItem(BCTransportPipes.woodPower);
        pipePowerCobble = makePipeItem(BCTransportPipes.cobblePower);
        pipePowerStone = makePipeItem(BCTransportPipes.stonePower);
        pipePowerQuartz = makePipeItem(BCTransportPipes.quartzPower);
        pipePowerGold = makePipeItem(BCTransportPipes.goldPower);
        // pipePowerIron = makePipeItem(BCTransportPipes.ironPower);
        pipePowerSandstone = makePipeItem(BCTransportPipes.sandstonePower);

        plugBlocker = ItemManager.register(new ItemPluggableSimple("item.plug.blocker", BCTransportPlugs.blocker));
        plugGate = ItemManager.register(new ItemPluggableGate("item.plug.gate"));
        plugPulsar = ItemManager.register(new ItemPluggablePulsar("item.plug.pulsar"));

        wire = ItemManager.register(new ItemWire("item.wire"));
    }

    public static ItemPipeHolder makePipeItem(PipeDefinition def) {
        ItemPipeHolder item = ItemManager.register(new ItemPipeHolder(def));
        if (item != null) {
            item.registerWithPipeApi();
        }
        return item;
    }
}

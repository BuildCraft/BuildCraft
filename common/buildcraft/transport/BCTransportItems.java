package buildcraft.transport;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.item.ItemManager;
import buildcraft.transport.api_move.PipeDefinition;
import buildcraft.transport.item.ItemPipeHolder;
import buildcraft.transport.item.ItemPluggableGate;
import buildcraft.transport.item.ItemPluggablePulsar;
import buildcraft.transport.item.ItemPluggableSimple;

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

    public static ItemPluggableSimple plugBlocker;
    public static ItemPluggableGate plugGate;
    public static ItemPluggablePulsar plugPulsar;

    public static void preInit() {
        waterproof = ItemManager.register(new ItemBC_Neptune("item.waterproof"));

        // Register them in order of type -- item, fluid, power

        pipeStructure = makePipeItem(BCTransportPipes.structure);

        pipeItemWood = makePipeItem(BCTransportPipes.woodItem);
        pipeItemCobble = makePipeItem(BCTransportPipes.cobbleItem);
        pipeItemStone = makePipeItem(BCTransportPipes.stoneItem);
        pipeItemQuartz = makePipeItem(BCTransportPipes.quartzItem);
        pipeItemGold = makePipeItem(BCTransportPipes.goldItem);
        pipeItemIron = makePipeItem(BCTransportPipes.ironItem);
        pipeItemSandstone = makePipeItem(BCTransportPipes.sandstoneItem);

        pipeFluidWood = makePipeItem(BCTransportPipes.woodFluid);
        pipeFluidCobble = makePipeItem(BCTransportPipes.cobbleFluid);
        pipeFluidStone = makePipeItem(BCTransportPipes.stoneFluid);
        pipeFluidQuartz = makePipeItem(BCTransportPipes.quartzFluid);
        pipeFluidGold = makePipeItem(BCTransportPipes.goldFluid);
        pipeFluidIron = makePipeItem(BCTransportPipes.ironFluid);
        pipeFluidSandstone = makePipeItem(BCTransportPipes.sandstoneFluid);

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
    }

    public static ItemPipeHolder makePipeItem(PipeDefinition def) {
        ItemPipeHolder item = ItemManager.register(new ItemPipeHolder(def));
        if (item != null) {
            item.registerWithPipeApi();
        }
        return item;
    }
}

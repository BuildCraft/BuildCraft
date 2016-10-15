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

    public static ItemPipeHolder pipeItemWood;
    public static ItemPipeHolder pipeFluidWood;
    public static ItemPipeHolder pipePowerWood;

    public static ItemPipeHolder pipeItemStone;
    public static ItemPipeHolder pipeFluidStone;
    public static ItemPipeHolder pipePowerStone;

    public static ItemPipeHolder pipeItemGold;
    public static ItemPipeHolder pipeFluidGold;
    public static ItemPipeHolder pipePowerGold;

    public static ItemPipeHolder pipeItemSandstone;
    public static ItemPipeHolder pipeFluidSandstone;
    public static ItemPipeHolder pipePowerSandstone;

    public static ItemPluggableSimple plugBlocker;
    public static ItemPluggableGate plugGate;
    public static ItemPluggablePulsar plugPulsar;

    public static void preInit() {
        waterproof = ItemManager.register(new ItemBC_Neptune("item.waterproof"));

        pipeItemWood = makePipeItem(BCTransportPipes.woodItem);
        pipeItemStone = makePipeItem(BCTransportPipes.stoneItem);
        pipeItemGold = makePipeItem(BCTransportPipes.goldItem);

        pipeFluidWood = makePipeItem(BCTransportPipes.woodFluid);
        pipeFluidStone = makePipeItem(BCTransportPipes.stoneFluid);
        pipeFluidGold = makePipeItem(BCTransportPipes.goldFluid);

        pipePowerWood = makePipeItem(BCTransportPipes.woodPower);
        pipePowerStone = makePipeItem(BCTransportPipes.stonePower);
        pipePowerGold = makePipeItem(BCTransportPipes.goldPower);

        pipeItemSandstone = makePipeItem(BCTransportPipes.sandstoneItem);
        pipeFluidSandstone = makePipeItem(BCTransportPipes.sandstoneFluid);
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

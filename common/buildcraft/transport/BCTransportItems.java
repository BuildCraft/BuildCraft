package buildcraft.transport;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.item.ItemManager;
import buildcraft.transport.item.ItemPipeHolder;

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

    public static void preInit() {
        waterproof = ItemManager.register(new ItemBC_Neptune("item.waterproof"));

        pipeItemWood = ItemManager.register(new ItemPipeHolder(BCTransportPipes.woodItem));
        pipeItemStone = ItemManager.register(new ItemPipeHolder(BCTransportPipes.stoneItem));
        pipeItemGold = ItemManager.register(new ItemPipeHolder(BCTransportPipes.goldItem));

        pipeFluidWood = ItemManager.register(new ItemPipeHolder(BCTransportPipes.woodFluid));
        pipeFluidStone = ItemManager.register(new ItemPipeHolder(BCTransportPipes.stoneFluid));
        pipeFluidGold = ItemManager.register(new ItemPipeHolder(BCTransportPipes.goldFluid));

        pipePowerWood = ItemManager.register(new ItemPipeHolder(BCTransportPipes.woodPower));
        pipePowerStone = ItemManager.register(new ItemPipeHolder(BCTransportPipes.stonePower));
        pipePowerGold = ItemManager.register(new ItemPipeHolder(BCTransportPipes.goldPower));
    }
}

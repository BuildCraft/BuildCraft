package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Slot;

public class ContainerSteamEngine extends Container
{

    public ContainerSteamEngine(InventoryPlayer inventoryplayer, TileEngine tileEngine)
    {
        burnTime = 0;
        itemBurnTime = 0;
        engine = tileEngine;
        addSlot(new Slot(tileEngine, 0, 80, 41));
        
        for(int i = 0; i < 3; i++)
        {
            for(int k = 0; k < 9; k++)
            {
                addSlot(new Slot(inventoryplayer, k + i * 9 + 9, 8 + k * 18, 84 + i * 18));
            }

        }

        for(int j = 0; j < 9; j++)
        {
            addSlot(new Slot(inventoryplayer, j, 8 + j * 18, 142));
        }

    }

//    public void updateCraftingResults()
//    {
//        super.updateCraftingResults();
//        for(int i = 0; i < field_20121_g.size(); i++)
//        {
//            ICrafting icrafting = (ICrafting)field_20121_g.get(i);
//            if(burnTime != engine.burnTime)
//            {
//                icrafting.func_20158_a(this, 0, engine.burnTime);
//            }
//            if(itemBurnTime != engine.totalBurnTime)
//            {
//                icrafting.func_20158_a(this, 1, engine.totalBurnTime);
//            }
//        }
//
//        burnTime = engine.burnTime;
//        itemBurnTime = engine.totalBurnTime;
//    }

    public void func_20112_a(int i, int j)
    {
        if(i == 0)
        {
            engine.burnTime = j;
        }
        if(i == 1)
        {
        	engine.totalBurnTime = j;
        }
       
    }

    public boolean isUsableByPlayer(EntityPlayer entityplayer)
    {
        return engine.canInteractWith(entityplayer);
    }

    private TileEngine engine;
    private int burnTime;
    private int itemBurnTime;

	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
}

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Slot;

public class ContainerSteamEngineRoot extends Container
{

    public ContainerSteamEngineRoot(InventoryPlayer inventoryplayer, TileEngine tileEngine)
    {
        burnTime = 0;
        totalBurnTime = 0;
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

    protected TileEngine engine;
    protected int burnTime;
    protected int totalBurnTime;

	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
}

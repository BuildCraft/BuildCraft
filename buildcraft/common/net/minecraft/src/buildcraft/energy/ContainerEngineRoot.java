package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Slot;

public class ContainerEngineRoot extends Container
{

    public ContainerEngineRoot(InventoryPlayer inventoryplayer, TileEngine tileEngine)
    {
        scaledBurnTime = 0;
        engine = tileEngine;
        
        if (tileEngine.engine instanceof EngineStone) {
        	addSlot(new Slot(tileEngine, 0, 80, 41)); 
        } else {
        	addSlot(new Slot(tileEngine, 0, 52, 41));
        }
        
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
        	System.out.println ("RECEIVED " + j);
            engine.scaledBurnTime = (short) j;
        }
       
    }

    public boolean isUsableByPlayer(EntityPlayer entityplayer)
    {
        return engine.canInteractWith(entityplayer);
    }

    protected TileEngine engine;
    protected int scaledBurnTime;

	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
}

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.ICrafting;
import net.minecraft.src.InventoryPlayer;

public class ContainerSteamEngine extends ContainerSteamEngineRoot
{

    public ContainerSteamEngine(InventoryPlayer inventoryplayer, TileEngine tileEngine) {
    	super (inventoryplayer, tileEngine);
    }

    @Override
	public void updateCraftingResults()
    {
        super.updateCraftingResults();
        for(int i = 0; i < field_20121_g.size(); i++)
        {
            ICrafting icrafting = (ICrafting)field_20121_g.get(i);
            if(burnTime != engine.burnTime)
            {
                icrafting.func_20158_a(this, 0, engine.burnTime);
            }
            if(totalBurnTime != engine.totalBurnTime)
            {
                icrafting.func_20158_a(this, 1, engine.totalBurnTime);
            }
        }

        burnTime = engine.burnTime;
        totalBurnTime = engine.totalBurnTime;
    }
    
    @Override
	public void func_20112_a(int i, int j) {		
		if (i == 0) {
			engine.burnTime = j;
		}
		
		if (i == 1) {
			engine.totalBurnTime = j;
		}
	}
}

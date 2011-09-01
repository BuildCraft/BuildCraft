package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.ICrafting;
import net.minecraft.src.InventoryPlayer;

public class ContainerEngine extends ContainerEngineRoot {

    public ContainerEngine(InventoryPlayer inventoryplayer,
			TileEngine tileEngine) {
		super(inventoryplayer, tileEngine);
		// TODO Auto-generated constructor stub
	}

    @Override
	public void updateCraftingResults()
    {
        super.updateCraftingResults();
        for(int i = 0; i < field_20121_g.size(); i++)
        {
            ICrafting icrafting = (ICrafting)field_20121_g.get(i);
            if(scaledBurnTime != engine.scaledBurnTime)
            {
                icrafting.func_20158_a(this, 0, engine.scaledBurnTime);
            }
        }

        scaledBurnTime = engine.scaledBurnTime;
    }
	
	@Override
	public void func_20112_a(int i, int j) {	
		if (i == 0) {			
			engine.scaledBurnTime = (short) j;
		}
	}
}

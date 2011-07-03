package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.ICrafting;
import net.minecraft.src.InventoryPlayer;

public class ContainerCombustionEngine extends ContainerCombustionEngineRoot {

    public ContainerCombustionEngine(InventoryPlayer inventoryplayer,
			TileEngine tileEngine) {
		super(inventoryplayer, tileEngine);
		// TODO Auto-generated constructor stub
	}

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
	
}

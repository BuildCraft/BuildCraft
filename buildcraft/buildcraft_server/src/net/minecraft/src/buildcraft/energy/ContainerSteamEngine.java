package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.ICrafting;
import net.minecraft.src.InventoryPlayer;

public class ContainerSteamEngine extends ContainerSteamEngineRoot {

    public ContainerSteamEngine(InventoryPlayer inventoryplayer,
			TileEngine tileEngine) {
		super(inventoryplayer, tileEngine);
		// TODO Auto-generated constructor stub
	}

	public void onCraftGuiOpened(ICrafting icrafting) {
        super.onCraftGuiOpened(icrafting);
        icrafting.updateCraftingInventoryInfo(this, 0, engine.burnTime);
        icrafting.updateCraftingInventoryInfo(this, 1, engine.totalBurnTime);
    }
	
	public void updateCraftingMatrix() {
        super.updateCraftingMatrix();
        for(int i = 0; i < crafters.size(); i++) {
            ICrafting icrafting = (ICrafting)crafters.get(i);
            if(burnTime != engine.burnTime) {
				icrafting.updateCraftingInventoryInfo(this, 0,
						engine.burnTime);
            }
            
            if(totalBurnTime != engine.totalBurnTime) {
				icrafting.updateCraftingInventoryInfo(this, 1,
						engine.totalBurnTime);
            }

        }

        burnTime = engine.burnTime;
        totalBurnTime = engine.totalBurnTime;
    }
	
}

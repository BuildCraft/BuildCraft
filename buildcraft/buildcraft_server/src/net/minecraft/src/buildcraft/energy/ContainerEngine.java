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
	public void onCraftGuiOpened(ICrafting icrafting) {
        super.onCraftGuiOpened(icrafting);
        icrafting.updateCraftingInventoryInfo(this, 0, engine.scaledBurnTime);
    }
	
	@Override
	public void updateCraftingMatrix() {
        super.updateCraftingMatrix();
        for(int i = 0; i < crafters.size(); i++) {
            ICrafting icrafting = (ICrafting)crafters.get(i);
            if(scaledBurnTime != engine.scaledBurnTime) {
				icrafting.updateCraftingInventoryInfo(this, 0,
						engine.scaledBurnTime);
            }
        }

        
        scaledBurnTime = engine.scaledBurnTime;
    }
	
}

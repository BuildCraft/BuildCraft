package buildcraft.core.gui.slots;

import net.minecraft.inventory.IInventory;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class SlotLimited extends SlotBase {
	
	private final int limit;
	
	public SlotLimited(IInventory iinventory, int slotIndex, int posX, int posY, int limit) {
		super(iinventory, slotIndex, posX, posY);
		this.limit = limit;
	}

	@Override
	public int getSlotStackLimit() {
		return limit;
	}
	
	
}

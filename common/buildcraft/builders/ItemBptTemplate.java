package buildcraft.builders;

import buildcraft.BuildCraftBuilders;
import buildcraft.core.BptBase;

public class ItemBptTemplate extends ItemBptBase {

	public ItemBptTemplate(int i) {
		super(i);
	}

	@Override
	public int getIconFromDamage(int i) {
		BptBase bpt = BuildCraftBuilders.getBptRootIndex().getBluePrint(i);
		if (bpt == null) {
			return 5 * 16 + 0;
		} else {
			return 5 * 16 + 1;
		}
	}
}

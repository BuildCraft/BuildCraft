package buildcraft.builders;

import buildcraft.BuildCraftBuilders;
import buildcraft.core.blueprints.BptBase;

public class ItemBptTemplate extends ItemBptBase {

	public ItemBptTemplate(int i) {
		super(i);
	}

	@Override
	public int getIconFromDamage(int i) {
		if (i == 0) {
			return 5 * 16 + 0;
		} else {
			return 5 * 16 + 1;
		}
	}
}

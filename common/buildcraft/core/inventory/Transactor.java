package buildcraft.core.inventory;

import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.utils.Utils;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public abstract class Transactor implements ITransactor {

	@Override
	public ItemStack add(ItemStack stack, ForgeDirection orientation, boolean doAdd) {
		ItemStack added = stack.copy();
		added.stackSize = inject(stack, orientation, doAdd);
		return added;
	}

	public abstract int inject(ItemStack stack, ForgeDirection orientation, boolean doAdd);

	public static ITransactor getTransactorFor(Object object) {

		if (object instanceof ISpecialInventory)
			return new TransactorSpecial((ISpecialInventory) object);
		else if (object instanceof ISidedInventory)
			return new TransactorSimple((ISidedInventory) object);
		else if (object instanceof IInventory)
			return new TransactorSimple(Utils.getInventory((IInventory) object));

		return null;
	}
}

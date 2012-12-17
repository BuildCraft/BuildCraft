package buildcraft.core.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.utils.Utils;

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

		// Furnaces need to be special cased to prevent vanilla XP exploits.
		else if (object instanceof TileEntityFurnace)
			return new TransactorFurnace((ISidedInventory) object);

		else if (object instanceof ISidedInventory)
			return new TransactorSided((ISidedInventory) object);

		else if (object instanceof IInventory)
			return new TransactorSimple(Utils.getInventory((IInventory) object));

		return null;
	}
}

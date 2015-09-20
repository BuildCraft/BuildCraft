package buildcraft.core.list;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.DimensionManager;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.core.proxy.CoreProxy;

public class ListMatchHandlerArmor extends ListMatchHandler {
	private int getArmorTypeID(ItemStack stack) {
		EntityPlayer player = CoreProxy.proxy.getClientPlayer();
		if (player == null) {
			player = CoreProxy.proxy.getBuildCraftPlayer(DimensionManager.getWorld(0)).get();
		}
		int atID = 0;

		for (int i = 0; i <= 3; i++) {
			if (stack.getItem().isValidArmor(stack, i, player)) {
				atID |= 1 << i;
			}
		}

		return atID;
	}

	@Override
	public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
		if (type == Type.TYPE) {
			int armorTypeIDSource = getArmorTypeID(stack);
			if (armorTypeIDSource > 0) {
				int armorTypeIDTarget = getArmorTypeID(target);
				if (precise) {
					return armorTypeIDSource == armorTypeIDTarget;
				} else {
					return (armorTypeIDSource & armorTypeIDTarget) != 0;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isValidSource(Type type, ItemStack stack) {
		return getArmorTypeID(stack) > 0;
	}
}

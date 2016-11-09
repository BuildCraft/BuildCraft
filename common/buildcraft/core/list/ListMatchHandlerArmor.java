package buildcraft.core.list;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.DimensionManager;

import buildcraft.api.lists.ListMatchHandler;

import buildcraft.lib.BCLibProxy;
import buildcraft.lib.misc.FakePlayerUtil;

public class ListMatchHandlerArmor extends ListMatchHandler {
    private static int getArmorTypeID(ItemStack stack) {
        EntityPlayer player = BCLibProxy.getProxy().getClientPlayer();
        if (player == null) {
            player = FakePlayerUtil.INSTANCE.getBuildCraftPlayer(DimensionManager.getWorld(0), BlockPos.ORIGIN).get();
        }
        int atID = 0;

        for (EntityEquipmentSlot i : EntityEquipmentSlot.values()) {
            if (stack.getItem().isValidArmor(stack, i, player)) {
                atID |= 1 << i.ordinal();
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

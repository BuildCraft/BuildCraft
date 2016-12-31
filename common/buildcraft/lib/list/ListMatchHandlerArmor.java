package buildcraft.lib.list;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.DimensionManager;

import buildcraft.api.lists.ListMatchHandler;

import buildcraft.lib.BCLibProxy;
import buildcraft.lib.misc.FakePlayerUtil;

public class ListMatchHandlerArmor extends ListMatchHandler {
    private static EnumSet<EntityEquipmentSlot> getArmorTypes(ItemStack stack) {
        EntityPlayer player = BCLibProxy.getProxy().getClientPlayer();
        if (player == null) {
            player = FakePlayerUtil.INSTANCE.getBuildCraftPlayer(DimensionManager.getWorld(0));
        }
        EnumSet<EntityEquipmentSlot> types = EnumSet.noneOf(EntityEquipmentSlot.class);

        for (EntityEquipmentSlot e : EntityEquipmentSlot.values()) {
            if (e.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                if (stack.getItem().isValidArmor(stack, e, player)) {
                    types.add(e);
                }
            }
        }

        return types;
    }

    @Override
    public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
            EnumSet<EntityEquipmentSlot> armorTypeIDSource = getArmorTypes(stack);
            if (armorTypeIDSource.size() > 0) {
                EnumSet<EntityEquipmentSlot> armorTypeIDTarget = getArmorTypes(target);
                if (precise) {
                    return armorTypeIDSource.equals(armorTypeIDTarget);
                } else {
                    armorTypeIDSource.removeAll(EnumSet.complementOf(armorTypeIDTarget));
                    return armorTypeIDSource.size() > 0;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, ItemStack stack) {
        return getArmorTypes(stack).size() > 0;
    }
}

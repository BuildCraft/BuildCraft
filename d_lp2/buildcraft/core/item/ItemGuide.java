package buildcraft.core.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import buildcraft.core.CoreGuis;
import buildcraft.lib.item.ItemBuildCraft_BC8;

public class ItemGuide extends ItemBuildCraft_BC8 {
    public ItemGuide(String id) {
        super(id);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        CoreGuis.GUIDE.openGUI(player);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}

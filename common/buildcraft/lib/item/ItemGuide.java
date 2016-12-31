package buildcraft.lib.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import buildcraft.lib.BCLib;

public class ItemGuide extends ItemBC_Neptune {
    public ItemGuide(String id) {
        super(id);
        setContainerItem(this);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        player.openGui(BCLib.INSTANCE, 0, world, 0, 0, 0);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
}

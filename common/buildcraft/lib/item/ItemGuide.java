package buildcraft.lib.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import buildcraft.lib.BuildCraftLib;

public class ItemGuide extends ItemBuildCraft_BC8 {
    public ItemGuide(String id) {
        super(id);
        setContainerItem(this);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        player.openGui(BuildCraftLib.INSTANCE, 0, world, 0, 0, 0);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}

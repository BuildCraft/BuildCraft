package buildcraft.lib.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import buildcraft.lib.BCLib;
import buildcraft.lib.misc.NBTUtilBC;

public class ItemGuideNote extends ItemBC_Neptune {

    public ItemGuideNote(String id) {
        super(id);
    }

    public static String getNoteId(ItemStack stack) {
        return NBTUtilBC.getItemData(stack).getString("note_id");
    }

    public ItemStack storeNoteId(String noteId) {
        ItemStack stack = new ItemStack(this);
        NBTUtilBC.getItemData(stack).setString("note_id", noteId);
        return stack;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStack, World world, EntityPlayer player, EnumHand hand) {
        player.openGui(BCLib.INSTANCE, 1, world, 0, 0, 0);
        return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
    }
}

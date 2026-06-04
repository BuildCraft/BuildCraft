package buildcraft.lib.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;
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
        NBTUtilBC.getItemData(stack).putString("note_id", noteId);
        return stack;
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public TypedActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        // STUB(R.Chen): Forge player.openGui → Fabric ScreenHandler, deferred — Phase 10
        return new ActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
    }
}

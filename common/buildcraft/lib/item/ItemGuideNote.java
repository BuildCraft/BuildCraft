package buildcraft.lib.item;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemGuideNote extends ItemBC_Neptune implements INamedContainerProvider {

    public ItemGuideNote(String idBC, Item.Properties properties) {
        super(idBC, properties);
    }

    public static String getNoteId(ItemStack stack) {
        return NBTUtilBC.getItemData(stack).getString("note_id");
    }

    public ItemStack storeNoteId(String noteId) {
        ItemStack stack = new ItemStack(this);
        NBTUtilBC.getItemData(stack).putString("note_id", noteId);
        return stack;
    }

    @Override
//    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
//        player.openGui(BCLib.INSTANCE, 1, world, 0, 0, 0);
        MessageUtil.serverOpenItemGui(player, this);
//        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        return new ActionResult<>(ActionResultType.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.getDisplayName();
    }

    // TODO Calen GuideNote gui not impl in 1.12.2
    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
        return null;
    }
}

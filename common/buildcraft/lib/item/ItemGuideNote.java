package buildcraft.lib.item;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ItemGuideNote extends ItemBC_Neptune implements MenuProvider {

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
//    public ActionResult<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand)
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
//        player.openGui(BCLib.INSTANCE, 1, world, 0, 0, 0);
        MessageUtil.serverOpenItemGui(player, this);
//        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    public Component getDisplayName() {
        return this.getDisplayName();
    }

    // TODO Calen GuideNote gui not impl in 1.12.2
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
        return null;
    }
}

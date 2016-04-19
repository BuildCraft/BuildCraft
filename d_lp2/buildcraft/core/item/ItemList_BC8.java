package buildcraft.core.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.items.IList;
import buildcraft.core.CoreGuis;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.lib.item.ItemBuildCraft_BC8;
import buildcraft.lib.list.ListHandler;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ItemList_BC8 extends ItemBuildCraft_BC8 implements IList {
    public ItemList_BC8(String id) {
        super(id);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        CoreGuis.LIST.openGUI(player);
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        addVariant(variants, 0, "clean");
        addVariant(variants, 1, "used");
    }

    @Override
    public int getMetadata(ItemStack stack) {
        return ListHandler.hasItems(stack) ? 1 : 0;
    }

    // IList

    @Override
    public String getName(ItemStack stack) {
        return NBTUtils.getItemData(stack).getString("label");
    }

    @Override
    public boolean setName(ItemStack stack, String name) {
        NBTUtils.getItemData(stack).setString("label", name);
        return true;
    }

    @Override
    public boolean matches(ItemStack stackList, ItemStack item) {
        return ListHandler.matches(stackList, item);
    }
}

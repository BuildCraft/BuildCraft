package buildcraft.silicon.item;

import java.util.List;

import javax.annotation.Nonnull;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;

public class ItemGateCopier extends ItemBC_Neptune {
    private static final String NBT_DATA = "gate_data";

    public ItemGateCopier(String id) {
        super(id);
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        addVariant(variants, 0, "empty");
        addVariant(variants, 1, "full");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        if (getMetadata(stack) != 0) {
            tooltip.add(LocaleUtil.localize("buildcraft.item.nonclean.usage"));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (player.isSneaking()) {
            return clearData(StackUtil.asNonNull(stack));
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    private ActionResult<ItemStack> clearData(@Nonnull ItemStack stack) {
        if (getMetadata(stack) == 0) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        NBTTagCompound nbt = NBTUtilBC.getItemData(stack);
        nbt.removeTag(NBT_DATA);
        if (nbt.hasNoTags()) {
            stack.setTagCompound(null);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public int getMetadata(ItemStack stack) {
        return getCopiedGateData(stack) != null ? 1 : 0;
    }

    public static NBTTagCompound getCopiedGateData(ItemStack stack) {
        return stack.getSubCompound(NBT_DATA);
    }

    public static void setCopiedGateData(ItemStack stack, NBTTagCompound nbt) {
        NBTUtilBC.getItemData(stack).setTag(NBT_DATA, nbt);
    }
}

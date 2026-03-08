package buildcraft.silicon.item;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemGateCopier extends ItemBC_Neptune {
    private static final String NBT_DATA = "gate_data";

    public ItemGateCopier(String idBC, Item.Properties properties) {
        super(idBC, properties);
//        setMaxStackSize(1);
    }

//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
//        addVariant(variants, 0, "empty");
//        addVariant(variants, 1, "full");
//    }

    @Override
    @OnlyIn(Dist.CLIENT)
//    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag)
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        if (getMetadata(stack) != 0) {
//            tooltip.add(LocaleUtil.localize("buildcraft.item.nonclean.usage"));
            tooltip.add(new TranslationTextComponent("buildcraft.item.nonclean.usage"));
        }
    }

    @Override
//    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) {
            return new ActionResult<>(ActionResultType.PASS, stack);
        }
        if (player.isShiftKeyDown()) {
            return clearData(StackUtil.asNonNull(stack));
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    private ActionResult<ItemStack> clearData(@Nonnull ItemStack stack) {
        if (getMetadata(stack) == 0) {
            return new ActionResult<>(ActionResultType.PASS, stack);
        }
        CompoundNBT nbt = NBTUtilBC.getItemData(stack);
        nbt.remove(NBT_DATA);
        if (nbt.isEmpty()) {
            stack.setTag(null);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    // @Override
    // public int getMetadata(ItemStack stack)
    public static int getMetadata(ItemStack stack) {
        return getCopiedGateData(stack) != null ? 1 : 0;
    }

    public static CompoundNBT getCopiedGateData(ItemStack stack) {
        return stack.getTagElement(NBT_DATA);
    }

    public static void setCopiedGateData(ItemStack stack, CompoundNBT nbt) {
        NBTUtilBC.getItemData(stack).put(NBT_DATA, nbt);
    }
}

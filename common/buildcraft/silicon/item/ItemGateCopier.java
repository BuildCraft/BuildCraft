package buildcraft.silicon.item;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
//    public void addInformation(ItemStack stack, Level world, List<String> tooltip, ITooltipFlag flag)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        if (getMetadata(stack) != 0) {
//            tooltip.add(LocaleUtil.localize("buildcraft.item.nonclean.usage"));
            tooltip.add(new TranslatableComponent("buildcraft.item.nonclean.usage"));
        }
    }

    @Override
//    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand)
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        if (player.isShiftKeyDown()) {
            return clearData(StackUtil.asNonNull(stack));
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    private InteractionResultHolder<ItemStack> clearData(@Nonnull ItemStack stack) {
        if (getMetadata(stack) == 0) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        CompoundTag nbt = NBTUtilBC.getItemData(stack);
        nbt.remove(NBT_DATA);
        if (nbt.isEmpty()) {
            stack.setTag(null);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    // @Override
    // public int getMetadata(ItemStack stack)
    public static int getMetadata(ItemStack stack) {
        return getCopiedGateData(stack) != null ? 1 : 0;
    }

    public static CompoundTag getCopiedGateData(ItemStack stack) {
        return stack.getTagElement(NBT_DATA);
    }

    public static void setCopiedGateData(ItemStack stack, CompoundTag nbt) {
        NBTUtilBC.getItemData(stack).put(NBT_DATA, nbt);
    }
}

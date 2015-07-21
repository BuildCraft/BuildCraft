package buildcraft.transport.stripes;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandler;

public class StripesHandlerShears implements IStripesHandler {

    @Override
    public StripesHandlerType getType() {
        return StripesHandlerType.ITEM_USE;
    }

    @Override
    public boolean shouldHandle(ItemStack stack) {
        return stack.getItem() instanceof ItemShears;
    }

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof IShearable) {
            IShearable shearableBlock = (IShearable) block;
            if (shearableBlock.isShearable(stack, world, pos)) {
                world.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), Block.soundTypeGrass.getBreakSound(), 1, 1);
                List<ItemStack> drops = shearableBlock.onSheared(stack, world, pos, EnchantmentHelper.getEnchantmentLevel(
                        Enchantment.fortune.effectId, stack));
                world.setBlockToAir(pos);
                if (stack.attemptDamageItem(1, player.getRNG())) {
                    stack.stackSize--;
                }
                if (stack.stackSize > 0) {
                    activator.sendItem(stack, direction.getOpposite());
                }
                for (ItemStack dropStack : drops) {
                    activator.sendItem(dropStack, direction.getOpposite());
                }
                return true;
            }
        }

        return false;
    }

}

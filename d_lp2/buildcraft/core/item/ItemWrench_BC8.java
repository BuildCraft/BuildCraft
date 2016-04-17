package buildcraft.core.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.api.tools.IToolWrench;
import buildcraft.lib.item.ItemBuildCraft_BC8;

public class ItemWrench_BC8 extends ItemBuildCraft_BC8 implements IToolWrench {
    public ItemWrench_BC8(String id) {
        super(id);
    }

    @Override
    public boolean canWrench(EntityPlayer player, BlockPos pos) {
        return true;
    }

    @Override
    public void wrenchUsed(EntityPlayer player, BlockPos pos) {
        player.swingArm(player.getActiveHand());
    }

    @Override
    public boolean canWrench(EntityPlayer player, Entity entity) {
        return true;
    }

    @Override
    public void wrenchUsed(EntityPlayer player, Entity entity) {
        player.swingArm(player.getActiveHand());
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        IBlockState state = world.getBlockState(pos);
        state = state.getBlock().getActualState(state, world, pos);
        return CustomRotationHelper.INSTANCE.attemptRotateBlock(world, pos, state, side);
    }
}

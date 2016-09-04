package buildcraft.factory.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.block.BlockWaterGel;
import buildcraft.factory.block.BlockWaterGel.GelStage;
import buildcraft.lib.item.ItemBC_Neptune;

public class ItemWaterGel extends ItemBC_Neptune {

    public ItemWaterGel(String id) {
        super(id);
        this.maxStackSize = 16;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {

        Vec3d start = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        Vec3d look = player.getLookVec();
        Vec3d end = start.add(look.scale(7));
        RayTraceResult ray = world.rayTraceBlocks(start, end, true, false, true);

        if (ray == null || ray.getBlockPos() == null) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        Block b = world.getBlockState(ray.getBlockPos()).getBlock();
        if (b != Blocks.WATER) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        if (!player.capabilities.isCreativeMode) {
            --stack.stackSize;
        }

        // Same as ItemSnowball
        world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ,//
                SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL,//
                0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!world.isRemote) {
            world.setBlockState(ray.getBlockPos(), BCFactoryBlocks.waterGel.getDefaultState().withProperty(BlockWaterGel.PROP_STAGE, GelStage.SPREAD_0));
            world.scheduleUpdate(ray.getBlockPos(), BCFactoryBlocks.waterGel, 200);

            // TODO: Snowball stuff

            // EntitySnowball entitysnowball = new EntitySnowball(world, player);
            // entitysnowball.setHeadingFromThrower(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
            // world.spawnEntityInWorld(entitysnowball);
        }

        // player.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

}

package buildcraft.lib.misc;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.api.tools.IToolWrench;

public class EntityUtil {
    public static List<ItemStack> collectItems(World world, BlockPos around, double radius) {
        return collectItems(world, new Vec3d(around).addVector(0.5, 0.5, 0.5), radius);
    }

    public static List<ItemStack> collectItems(World world, Vec3d around, double radius) {
        List<ItemStack> stacks = new ArrayList<>();

        AxisAlignedBB aabb = BoundingBoxUtil.makeAround(around, radius);
        for (EntityItem ent : world.getEntitiesWithinAABB(EntityItem.class, aabb)) {
            if (!ent.isDead) {
                ent.isDead = true;
                stacks.add(ent.getEntityItem());
            }
        }
        return stacks;
    }

    public static Vec3d getVec(Entity entity) {
        return new Vec3d(entity.posX, entity.posY, entity.posZ);
    }

    public static EnumHand getWrenchHand(EntityLivingBase entity) {
        ItemStack stack = entity.getHeldItemMainhand();
        if (stack != null && stack.getItem() instanceof IToolWrench) {
            return EnumHand.MAIN_HAND;
        }
        stack = entity.getHeldItemOffhand();
        if (stack != null && stack.getItem() instanceof IToolWrench) {
            return EnumHand.OFF_HAND;
        }
        return null;
    }

    public static void activateWrench(EntityPlayer player) {
        ItemStack stack = player.getHeldItemMainhand();
        if (stack != null && stack.getItem() instanceof IToolWrench) {
            IToolWrench wrench = (IToolWrench) stack.getItem();
            wrench.wrenchUsed(player, EnumHand.MAIN_HAND, stack, null);
            return;
        }
        stack = player.getHeldItemOffhand();
        if (stack != null && stack.getItem() instanceof IToolWrench) {
            IToolWrench wrench = (IToolWrench) stack.getItem();
            wrench.wrenchUsed(player, EnumHand.OFF_HAND, stack, null);
        }
    }
}

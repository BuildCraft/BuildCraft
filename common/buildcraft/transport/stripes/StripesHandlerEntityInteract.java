/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.stripes;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;

public enum StripesHandlerEntityInteract implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator) {
        pos = pos.offset(direction);
        AxisAlignedBB box = new AxisAlignedBB(pos, pos.add(1, 1, 1));
        List<EntityLivingBase> livingEntities = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
        Collections.shuffle(livingEntities);
        while (livingEntities.size() > 0) {
            EntityLivingBase entity = livingEntities.remove(0);
            if (player.interactOn(entity, EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS) {
                return true;
            }
        }
        return false;
    }
}

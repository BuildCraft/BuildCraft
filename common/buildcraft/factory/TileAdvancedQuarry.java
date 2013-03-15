/**
 * Copyright (c) sadris, 2013
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory;

import buildcraft.api.power.IPowerProvider;
import buildcraft.core.Box;
import buildcraft.core.EntityRobot;
import buildcraft.core.blueprints.BptBuilderBase;
import buildcraft.core.network.TileNetworkData;
import buildcraft.core.proxy.CoreProxy;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;

public class TileAdvancedQuarry extends TileQuarry {

    public TileAdvancedQuarry() {
        super();
    }

    public void positionReached() {
        inProcess = false;

        if (worldObj.isRemote)
            return;

        int i = targetX;
        int j = targetY - 1;
        int k = targetZ;

        int blockId = worldObj.getBlockId(i, j, k);

        if (isQuarriableBlock(i, j, k)) {
            powerProvider.getTimeTracker().markTime(worldObj);

            // Share this with mining well!

            Block block = Block.blocksList[worldObj.getBlockId(i, j, k)];
            if( block!=null ) {
                mineStack( new ItemStack(block) );
            }

            worldObj.playAuxSFXAtEntity(null, 2001, i, j, k, blockId + (worldObj.getBlockMetadata(i, j, k) << 12));
            worldObj.setBlockWithNotify(i, j, k, 0);
        }

        // Collect any lost items laying around
        double[] head = getHead();
        AxisAlignedBB axis = AxisAlignedBB.getBoundingBox(head[0] - 2, head[1] - 2, head[2] - 2, head[0] + 3, head[1] + 3, head[2] + 3);
        List result = worldObj.getEntitiesWithinAABB(EntityItem.class, axis);
        for (int ii = 0; ii < result.size(); ii++) {
            if (result.get(ii) instanceof EntityItem) {
                EntityItem entity = (EntityItem) result.get(ii);
                if (entity.isDead) {
                    continue;
                }

                ItemStack mineable = entity.getEntityItem();
                if (mineable.stackSize <= 0) {
                    continue;
                }
                CoreProxy.proxy.removeEntity(entity);
                mineStack(mineable);
            }
        }
    }
}

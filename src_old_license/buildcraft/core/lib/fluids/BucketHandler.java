/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.fluids;

import java.util.Map;
import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class BucketHandler {

    public static BucketHandler INSTANCE = new BucketHandler();
    public Map<IBlockState, Item> buckets = Maps.newHashMap();

    private BucketHandler() {}

    @SubscribeEvent
    public void onBucketFill(FillBucketEvent event) {
        ItemStack result = fillCustomBucket(event.getWorld(), event.getTarget());

        if (result == null) {
            return;
        }

        event.setFilledBucket(result);
        event.setResult(Result.ALLOW);
    }

    private ItemStack fillCustomBucket(World world, RayTraceResult pos) {
        IBlockState state = world.getBlockState(pos.getBlockPos());

        Item bucket = buckets.get(state);

        if (bucket != null) {
            world.setBlockToAir(pos.getBlockPos());
            return new ItemStack(bucket);
        } else {
            return null;
        }
    }
}

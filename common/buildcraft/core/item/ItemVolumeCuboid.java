package buildcraft.core.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.core.marker.volume.VolumeMarkerCache;
import buildcraft.core.marker.volume.VolumeMarkerCache.VolumeBox;
import buildcraft.lib.item.ItemBC_Neptune;

public class ItemVolumeCuboid extends ItemBC_Neptune {
    public ItemVolumeCuboid(String id) {
        super(id);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }

        BlockPos offset = pos.offset(facing);

        VolumeBox current = VolumeMarkerCache.SERVER_INSTANCE.getBoxAt(offset);

        if (current == null) {
            VolumeMarkerCache.SERVER_INSTANCE.addBox(offset);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.FAIL;
    }
}

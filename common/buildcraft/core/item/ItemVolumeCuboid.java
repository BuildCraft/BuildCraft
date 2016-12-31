package buildcraft.core.item;

import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeMarkers;
import buildcraft.lib.item.ItemBC_Neptune;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemVolumeCuboid extends ItemBC_Neptune {
    public ItemVolumeCuboid(String id) {
        super(id);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }

        BlockPos offset = pos.offset(facing);

        WorldSavedDataVolumeMarkers volumeMarkers = WorldSavedDataVolumeMarkers.get(world);
        VolumeBox current = volumeMarkers.getBoxAt(offset);

        if (current == null) {
            volumeMarkers.addBox(offset);
            volumeMarkers.markDirty();
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.FAIL;
    }
}

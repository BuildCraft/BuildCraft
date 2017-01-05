package buildcraft.core.marker.volume;

import buildcraft.lib.item.ItemBC_Neptune;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

public abstract class ItemAddon extends ItemBC_Neptune {
    public ItemAddon(String id) {
        super(id);
    }

    public abstract Addon createAddon();

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }

        WorldSavedDataVolumeMarkers volumeMarkers = WorldSavedDataVolumeMarkers.get(world);
        Pair<VolumeBox, EnumAddonSlot> selectingBoxAndSlot = EnumAddonSlot.getSelectingBoxAndSlot(player, volumeMarkers);
        VolumeBox box = selectingBoxAndSlot.getLeft();
        EnumAddonSlot slot = selectingBoxAndSlot.getRight();
        if (box != null && slot != null) {
            if (!box.addons.containsKey(slot)) {
                box.addons.put(slot, createAddon());
                box.addons.get(slot).onAdded();
                volumeMarkers.markDirty();
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }
}

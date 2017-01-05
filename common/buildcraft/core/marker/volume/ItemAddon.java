package buildcraft.core.marker.volume;

import buildcraft.lib.item.ItemBC_Neptune;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
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
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
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
                return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
            }
        }

        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }
}

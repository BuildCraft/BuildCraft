package buildcraft.builders.container;

import buildcraft.core.marker.volume.*;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.misc.data.Box;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ContainerFillingPlanner extends ContainerBC_Neptune {
    public Box boxBox;
    public EnumAddonSlot slot;

    public ContainerFillingPlanner(EntityPlayer player) {
        super(player);
        Pair<VolumeBox, EnumAddonSlot> selectingBoxAndSlot = EnumAddonSlot.getSelectingBoxAndSlot(player, ClientVolumeMarkers.INSTANCE);
        boxBox = selectingBoxAndSlot.getLeft().box;
        slot = selectingBoxAndSlot.getRight();
    }

    public Addon getAddon() {
        List<VolumeBox> boxes = player.world.isRemote ? ClientVolumeMarkers.INSTANCE.boxes : WorldSavedDataVolumeMarkers.get(player.world).boxes;
        return boxes.stream()
                .filter(box -> box.box.equals(boxBox))
                .flatMap(box -> box.addons.entrySet().stream())
                .filter(slotAddon -> slotAddon.getKey() == slot)
                .findFirst()
                .orElse(null)
                .getValue();
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}

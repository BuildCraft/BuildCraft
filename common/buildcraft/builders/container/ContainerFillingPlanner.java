package buildcraft.builders.container;

import buildcraft.builders.addon.AddonFillingPlanner;
import buildcraft.builders.filling.IParameter;
import buildcraft.core.marker.volume.*;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ContainerFillingPlanner extends ContainerBC_Neptune {
    private static final IdAllocator IDS = ContainerBC_Neptune.IDS.makeChild("filling_planner");
    private static final int ID_PARAMETERS = IDS.allocId("PARAMETERS");

    public Box boxBox;
    public EnumAddonSlot slot;
    public List<IParameter> parameters = new ArrayList<>();

    public ContainerFillingPlanner(EntityPlayer player) {
        super(player);
        Pair<VolumeBox, EnumAddonSlot> selectingBoxAndSlot = EnumAddonSlot.getSelectingBoxAndSlot(player, ClientVolumeMarkers.INSTANCE);
        VolumeBox box = selectingBoxAndSlot.getLeft();
        boxBox = box.box;
        slot = selectingBoxAndSlot.getRight();
        parameters = ((AddonFillingPlanner) box.addons.get(slot)).parameters;
    }

    public AddonFillingPlanner getAddon() {
        List<VolumeBox> boxes = player.world.isRemote ? ClientVolumeMarkers.INSTANCE.boxes : WorldSavedDataVolumeMarkers.get(player.world).boxes;
        return (AddonFillingPlanner) boxes.stream()
                .filter(box -> box.box.equals(boxBox))
                .flatMap(box -> box.addons.entrySet().stream())
                .filter(slotAddon -> slotAddon.getKey() == slot)
                .findFirst()
                .orElse(null)
                .getValue();
    }

    public void sendParametersToServer() {
        sendMessage(ID_PARAMETERS, buffer -> {
            buffer.writeInt(parameters.size());
            parameters.forEach(parameter -> IParameter.toBytes(buffer, parameter));
        });
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == Side.SERVER) {
            if (id == ID_PARAMETERS) {
                parameters.clear();
                IntStream.range(0, buffer.readInt()).mapToObj(i -> IParameter.fromBytes(buffer)).forEach(parameters::add);
                getAddon().parameters = parameters;
                WorldSavedDataVolumeMarkers.get(player.world).markDirty();
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}

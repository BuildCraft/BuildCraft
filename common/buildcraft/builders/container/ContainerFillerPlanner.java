package buildcraft.builders.container;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.filler.IFillerPattern;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.FullStatement;

import buildcraft.builders.addon.AddonFillingPlanner;
import buildcraft.builders.filler.FillerType;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.ClientVolumeBoxes;
import buildcraft.core.marker.volume.EnumAddonSlot;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

public class ContainerFillerPlanner extends ContainerBC_Neptune {

    public final AddonFillingPlanner fillerPlanner;
    public final FullStatement<IFillerPattern> patternClient;

    public ContainerFillerPlanner(EntityPlayer player) {
        super(player);
        List<VolumeBox> boxes;
        if (!player.world.isRemote) {
            boxes = WorldSavedDataVolumeBoxes.get(player.world).boxes;
            MessageUtil.doDelayed(this::sendData);
        } else {
            boxes = ClientVolumeBoxes.INSTANCE.boxes;
        }
        Pair<VolumeBox, EnumAddonSlot> selectingBoxAndSlot = EnumAddonSlot.getSelectingBoxAndSlot(player, boxes);
        VolumeBox volume = selectingBoxAndSlot.getLeft();
        EnumAddonSlot slot = selectingBoxAndSlot.getRight();
        if (volume == null) {
            fillerPlanner = null;
            patternClient = null;
        } else {
            Map<EnumAddonSlot, Addon> addons = volume.addons;
            fillerPlanner = (AddonFillingPlanner) addons.get(slot);
            patternClient = new FullStatement<>(FillerType.INSTANCE, 4, this::onStatementChange);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    private void onStatementChange(FullStatement<?> statement, int paramIndex) {
        sendData();
    }

    public void setInverted(boolean value) {
        if (fillerPlanner == null) return;
        fillerPlanner.inverted = value;
        sendData();
    }

    private void sendData() {
        if (fillerPlanner == null) return;
        final FullStatement<IFillerPattern> patternStatement = player.world.isRemote
            ? patternClient
            : fillerPlanner.patternStatement;
        sendMessage(NET_DATA, buffer -> {
            patternStatement.writeToBuffer(buffer);
            buffer.writeBoolean(fillerPlanner.inverted);
        });
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        if (fillerPlanner == null) return;
        if (side == Side.SERVER) {
            if (id == NET_DATA) {
                fillerPlanner.patternStatement.readFromBuffer(buffer);
                fillerPlanner.inverted = buffer.readBoolean();
                fillerPlanner.updateBuildingInfo();
                WorldSavedDataVolumeBoxes.get(player.world).markDirty();
                sendData();
            }
        } else if (side == Side.CLIENT) {
            if (id == NET_DATA) {
                fillerPlanner.patternStatement.readFromBuffer(buffer);
                fillerPlanner.inverted = buffer.readBoolean();
                patternClient.set(fillerPlanner.patternStatement.get());
                for (int i = 0; i < 4; i++) {
                    patternClient.getParamRef(i).set(fillerPlanner.patternStatement.getParamRef(i).get());
                }
                fillerPlanner.updateBuildingInfo();
            }
        }
    }
}

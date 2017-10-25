package buildcraft.builders.container;

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.filler.IFillerPattern;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.FullStatement;

import buildcraft.builders.addon.AddonFillingPlanner;
import buildcraft.builders.filler.FillerType;
import buildcraft.core.marker.volume.ClientVolumeBoxes;
import buildcraft.core.marker.volume.EnumAddonSlot;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

public class ContainerFillerPlanner extends ContainerBC_Neptune implements IContainerFilling {
    public final AddonFillingPlanner addon;
    private final FullStatement<IFillerPattern> patternStatementClient = new FullStatement<>(
        FillerType.INSTANCE,
        4,
        (statement, paramIndex) -> onStatementChange()
    );

    public ContainerFillerPlanner(EntityPlayer player) {
        super(player);
        Pair<VolumeBox, EnumAddonSlot> selectingBoxAndSlot = EnumAddonSlot.getSelectingBoxAndSlot(
            player,
            !player.world.isRemote
                ? WorldSavedDataVolumeBoxes.get(player.world).boxes
                : ClientVolumeBoxes.INSTANCE.boxes
        );
        addon = Optional.ofNullable(selectingBoxAndSlot.getLeft())
            .map(volumeBox -> volumeBox.addons.get(selectingBoxAndSlot.getRight()))
            .map(AddonFillingPlanner.class::cast)
            .orElseThrow(IllegalStateException::new);
        init();
    }

    @Override
    public EntityPlayer getPlayer() {
        return player;
    }

    @Override
    public FullStatement<IFillerPattern> getPatternStatementClient() {
        return patternStatementClient;
    }

    @Override
    public FullStatement<IFillerPattern> getPatternStatement() {
        return addon.patternStatement;
    }

    @Override
    public boolean isInverted() {
        return addon.inverted;
    }

    @Override
    public void setInverted(boolean value) {
        addon.inverted = value;
    }

    @Override
    public void valuesChanged() {
        addon.updateBuildingInfo();
        if (!player.world.isRemote) {
            WorldSavedDataVolumeBoxes.get(getPlayer().world).markDirty();
        }
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        IContainerFilling.super.readMessage(id, buffer, side, ctx);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}

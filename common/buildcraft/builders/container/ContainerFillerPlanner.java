package buildcraft.builders.container;

import buildcraft.api.filler.IFillerPattern;
import buildcraft.builders.addon.AddonFillerPlanner;
import buildcraft.builders.filler.FillerType;
import buildcraft.builders.tile.TileFiller;
import buildcraft.core.BCCoreProxy;
import buildcraft.core.marker.volume.EnumAddonSlot;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.FullStatement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Optional;

//public class ContainerFillerPlanner extends ContainerBC_Neptune implements IContainerFilling
public class ContainerFillerPlanner extends ContainerBC_Neptune<TileFiller> implements IContainerFilling {
    public final AddonFillerPlanner addon;
    private final FullStatement<IFillerPattern> patternStatementClient = new FullStatement<>(
            FillerType.INSTANCE,
            4,
            (statement, paramIndex) -> onStatementChange()
    );

    public ContainerFillerPlanner(ContainerType menuType, int id, PlayerEntity player) {
        super(menuType, id, player);
        Pair<VolumeBox, EnumAddonSlot> selectingVolumeBoxAndSlot = EnumAddonSlot.getSelectingVolumeBoxAndSlot(
                player,
                BCCoreProxy.getProxy().getVolumeBoxes(player.level)
        );
        addon = Optional.ofNullable(selectingVolumeBoxAndSlot.getLeft())
                .map(volumeBox -> volumeBox.addons.get(selectingVolumeBoxAndSlot.getRight()))
                .map(AddonFillerPlanner.class::cast)
                .orElseThrow(IllegalStateException::new);
        init();
    }

    @Override
    public PlayerEntity getPlayer() {
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
        if (!player.level.isClientSide) {
            WorldSavedDataVolumeBoxes.get(getPlayer().level).setDirty();
        }
    }

    @Override
//    public void readMessage(int id, PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException
    public void readMessage(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        IContainerFilling.super.readMessage(id, buffer, side, ctx);
    }

    @SuppressWarnings("NullableProblems")
    @Override
//    public boolean canInteractWith(PlayerEntity player)
    public boolean stillValid(PlayerEntity player) {
        return true;
    }
}
